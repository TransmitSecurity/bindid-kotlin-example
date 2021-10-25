package com.ts.bindid.example.ui.main

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.nimbusds.jose.crypto.RSASSAVerifier
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jwt.SignedJWT
import com.ts.bindid.*
import com.ts.bindid.example.R
import com.ts.bindid.example.ui.main.token.TokenFragment
import com.ts.bindid.impl.XmBindIdErrorImpl
import com.ts.bindid.util.ObservableFuture
import com.ts.bindid.util.getRunningActivity
import kotlinx.android.synthetic.main.main_fragment.*
import okhttp3.*
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.io.IOException

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main_login_btn.isEnabled = false
        main_login_btn.setOnClickListener {
            authenticate(requireContext())
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Configure and initialize the BindID SDK
        initBindID(requireContext().applicationContext)
    }

    /**
     * Configure the BindID SDK with your client ID, and to work with the BindID sandbox environment
     */
    fun initBindID(context: Context){
        XmBindIdSdk.getInstance().initialize(
            XmBindIdConfig.create(
                context,
                XmBindIdServerEnvironment.createWithUrl(context.getString(R.string.bindid_host)),
                context.getString(R.string.bindid_client_id)
            )
        ).addListener(object : ObservableFuture.Listener<Boolean, XmBindIdError> {
            override fun onComplete(result: Boolean) {
                Timber.i("SDK initialized")
                main_progress_bar.visibility = View.INVISIBLE
                main_login_btn.isEnabled = true
            }

            override fun onReject(error: XmBindIdError) {
                Timber.e("SDK failed to initialize")
            }
        })
    }


    /**
     * Authenticate the user
     */
    fun authenticate(context: Context) {
        XmBindIdSdk.getInstance().authenticate(
            XmBindIdAuthenticationRequest.create(context.getString(R.string.bindid_redirect_uri))
                .apply {
                    this.usePkce = true
                    this.scope = listOf(XmBindIdScopeType.OpenId, XmBindIdScopeType.Email, XmBindIdScopeType.NetworkInfo)
                }).addListener(object : ObservableFuture.Listener<XmBindIdResponse, XmBindIdError> {
            override fun onComplete(response: XmBindIdResponse) {
                Timber.i("Authentication successful")
                // Do when using PKCE
                exchange(response)
            }

            override fun onReject(error: XmBindIdError) {
                onError(error)
            }
        })
    }

    /**
     * Exchange the authentication response for the ID and access token using a PKCE token exchange
     */
    fun exchange(response: XmBindIdResponse) {
        XmBindIdSdk.getInstance().exchangeToken(
            XmBindIdExchangeTokenRequest.create(response)
        ).addListener(object :
            ObservableFuture.Listener<XmBindIdExchangeTokenResponse, XmBindIdError> {
            override fun onComplete(tokenResponse: XmBindIdExchangeTokenResponse) {
                Timber.i("Token exchange successful")

                // Validate the tokenResponse
                // 1. get publicKey from BindID server
                // 2. validate JWT
                fetchBindIDPublicKey(object : fetchBindIDPublicKeyListener {
                    override fun onResponse(publicKey: String?) {
                        try {
                            val isValid = SignedJWT.parse(tokenResponse.idToken)
                                .verify(RSASSAVerifier(RSAKey.parse(publicKey)))

                            if(isValid){
                                // When connected to your company's backend, send the ID and
                                // access tokens to be processed
                                sendTokenToServer(tokenResponse.accessToken, tokenResponse.idToken)

                                Handler(Looper.getMainLooper()).post {
                                    // Once authentication and token exchange are done
                                    MainFragment.getRunningActivity()?.supportFragmentManager?.beginTransaction()
                                        ?.replace(R.id.container, TokenFragment.newInstance(tokenResponse.idToken))
                                        ?.commitNow()
                                }

                            } else {
                                val xmBindIdError: XmBindIdError = XmBindIdErrorImpl(
                                    XmBindIdErrorCode.InvalidResponse, "Invalid JWT signature"
                                )
                                onError(xmBindIdError)
                            }
                        } catch (e: Exception){
                            Timber.e(e)
                            val xmBindIdError: XmBindIdError = XmBindIdErrorImpl(
                                XmBindIdErrorCode.InvalidResponse, e.message!!
                            )
                            onError(xmBindIdError)

                        }
                    }

                    override fun onFailure(error: String?) {
                        val xmBindIdError: XmBindIdError = XmBindIdErrorImpl(
                            XmBindIdErrorCode.InvalidResponse, error ?: "Invalid JWT signature"
                        )
                        onError(xmBindIdError)
                    }

                })
            }

            override fun onReject(xmBindIdError: XmBindIdError) {
                onError(xmBindIdError)
            }
        })
    }

    // sendTokenToServer should send the ID and access tokens received upon successful authentication
    // to your backend server, where it will be processed
    fun sendTokenToServer(one: String, two: String) {
        // Add code to send the ID and access token to your application server here
    }

    private interface fetchBindIDPublicKeyListener {
        fun onResponse(publicKey: String?)
        fun onFailure(error: String?)
    }

    /**
     * Fetch the public key from the BindID jwks endpoint
     * @param listener
     */
    private fun fetchBindIDPublicKey(listener: fetchBindIDPublicKeyListener) {
        val client = OkHttpClient()
        val url = getString(R.string.bindid_host) + "/jwks"
        val request: Request = Request.Builder()
            .url(url)
            .build()
        client.newCall(request).enqueue(object : Callback {
            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                // Serialize the response and convert it to an array of key objects
                val responseData = response.body!!.string()
                var json: JSONObject? = null
                try {
                    json = JSONObject(responseData)
                    val keys = if (json.has("keys")) json.getJSONArray("keys") else null

                    // Find the key that contains the "sig" value in the "use" key. Return the publicKey in it
                    for (i in 0 until keys!!.length()) {
                        val key = keys.getJSONObject(i)
                        if (key["use"] == "sig") {
                            listener.onResponse(key.toString())
                            return
                        }
                    }
                    listener.onFailure("No signature key in publicKey")

                } catch (e: JSONException) {
                    listener.onFailure(e.message)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                listener.onFailure(e.message)
            }
        })
    }

    /**
     * Display an error message to the user
     */
   private fun onError(bindIdError: XmBindIdError) {
        val err: String = if (bindIdError.message === "")
            bindIdError.code.name
        else bindIdError.message + ": " + bindIdError.code.name
        Timber.e(err)
        Snackbar.make(requireView(), err, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(R.color.colorError))
            .show()
    }

}