package com.ts.bindid.example.ui.main.token

import android.content.Context
import org.json.JSONObject
import timber.log.Timber
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import com.ts.bindid.example.R
import com.ts.bindid.util.cast

/**
 * Created by Ran Stone on 23/06/2021.
 */
class TokenData(token: String) {

    private val json: JSONObject = JSONObject(token)

    private var userID: String? = null
    private var userAlias: String? = null
    private var phoneNumber: String? = null
    private var emailAddress: String? = null
    private var userRegisteredOn: String? = null
    private var userFirstSeen: String? = null
    private var userFirstConfirmed: String? = null
    private var userLastSeen: String? = null
    private var userLastSeenByNetwork: String? = null
    private var totalProvidersThatConfirmedUser: String? = null
    private var authenticatingDeviceRegistered: String? = null
    private var authenticatingDeviceFirstSeen: String? = null
    private var authenticatingDeviceConfirmed: String? = null
    private var authenticatingDeviceLastSeen: String? = null
    private var authenticatingDeviceLastSeenByNetwork: String? = null
    private var totalKnownDevices: String? = null

    init {
        Timber.i("json result: $json")
        val format = SimpleDateFormat("MMM d, yyyy HH:mm a")

        userID = json.opt("sub")?.toString()
        userAlias = json.opt("bindid_alias")?.toString() ?: "Not Set"
        phoneNumber = json.opt("phone_number")?.toString()
        emailAddress = json.opt("email")?.toString() ?: "Not Set"
        authenticatingDeviceConfirmed = json.opt("acr.ts.bindid.app_bound_cred")?.toString() ?: "No"

        // Network Info
        json.optJSONObject("bindid_network_info")?.let { json ->
            userRegisteredOn = json.optString("user_registration_time")
            userLastSeenByNetwork = json.optString("user_last_seen")
            totalKnownDevices = json.opt("device_count")?.toString() ?: "0"
            authenticatingDeviceLastSeenByNetwork =
                when(json.optString("authenticating_device_last_seen")){
                    "null" -> null
                    else -> json.optString("authenticating_device_last_seen")
                }
            totalProvidersThatConfirmedUser = json.opt("confirmed_capp_count")?.toString() ?: "0"
            authenticatingDeviceRegistered =
                json.optString("authenticating_device_registration_time")
        }

        // BindID Info
        json.optJSONObject("bindid_info")?.let { json ->
            userFirstSeen = json.opt("capp_first_login")?.toDateString(format)
            userFirstConfirmed = json.opt("capp_first_confirmed_login")?.toDateString(format)
            userLastSeen = json.opt("capp_last_login")?.toDateString(format)
            authenticatingDeviceFirstSeen =
                json.opt("capp_first_login_from_authenticating_device")?.toDateString(format)
            authenticatingDeviceLastSeen =
                json.opt("capp_last_login_from_authenticating_device")?.toDateString(format)
        }
    }

    fun getTokens(context: Context): List<TokenItem> {
        val list = mutableListOf<TokenItem>()
        userID?.let { list.add(TokenItem(context, R.string.ts_bindid_passport_user_id, it)) }
        userAlias?.let { list.add(TokenItem(context, R.string.ts_bindid_passport_user_alias, it)) }
        phoneNumber?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_phone_number,
                    it
                )
            )
        }
        emailAddress?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_email_address,
                    it
                )
            )
        }
        userRegisteredOn?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_user_registered_on,
                    it
                )
            )
        }
        userFirstSeen?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_user_first_seen,
                    it
                )
            )
        }
        userFirstConfirmed?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_user_first_confirmed,
                    it
                )
            )
        }
        userLastSeen?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_user_last_seen,
                    it
                )
            )
        }
        userLastSeenByNetwork?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_user_last_seen_by_network,
                    it
                )
            )
        }
        totalProvidersThatConfirmedUser?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_total_providers_that_confirmed_user,
                    it
                )
            )
        }
        authenticatingDeviceRegistered?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_authenticating_device_registered,
                    it
                )
            )
        }
        authenticatingDeviceFirstSeen?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_authenticating_device_first_seen,
                    it
                )
            )
        }
        authenticatingDeviceConfirmed?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_authenticating_device_confirmed,
                    it
                )
            )
        }
        authenticatingDeviceLastSeen?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_authenticating_device_last_seen,
                    it
                )
            )
        }
        authenticatingDeviceLastSeenByNetwork?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_authenticating_device_last_seen_by_network,
                    it
                )
            )
        }
        totalKnownDevices?.let {
            list.add(
                TokenItem(
                    context,
                    R.string.ts_bindid_passport_total_known_devices,
                    it
                )
            )
        }
        return list
    }

    private fun Long.toDate(): Date {
        return Date(this * 1000L)
    }

    private fun Number.toDateString(format: DateFormat = SimpleDateFormat("MMM d, yyyy HH:mm a")): String {
        return format.format(this.toLong().toDate())
    }

    private fun Any.toDateString(format: DateFormat = SimpleDateFormat("MMM d, yyyy HH:mm a")): String? {
        return this.cast<Number>()?.let { it.toDateString(format) }
    }
}
