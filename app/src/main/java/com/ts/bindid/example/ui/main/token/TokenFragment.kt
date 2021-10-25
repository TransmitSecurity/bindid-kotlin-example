package com.ts.bindid.example.ui.main.token

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.nimbusds.jwt.SignedJWT
import com.ts.bindid.example.R
import kotlinx.android.synthetic.main.token_fragment.*

private const val RESPONSE_ID_TOKEN = "id_token"

class TokenFragment : Fragment() {

    private var mIdToken: String? = null

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            TokenFragment().apply {
                arguments = Bundle().apply {
                    putString(RESPONSE_ID_TOKEN, param1)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            mIdToken = it.getString(RESPONSE_ID_TOKEN)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.token_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val signedJWT = SignedJWT.parse(mIdToken)
        token_values_rv.apply {
            // Get the JWT token and display it in a user friendly format
            val jsonData: String = signedJWT.payload.toString()
            val tokenData = TokenData(jsonData)
            adapter = PassportAdapter(tokenData.getTokens(requireContext()))
        }
    }

}

data class TokenItem(val name: String, val value: String) {
    constructor(context: Context, @StringRes name: Int, value: String) : this(context.getString(name), value)
}

