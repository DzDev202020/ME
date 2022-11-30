package com.amk.me.model.retrofit

import android.content.SharedPreferences

class SessionManager(
    var sharedPreferences: SharedPreferences,
    var sessionWebService: SessionWebService
) {


    /**
     * Keys used to save shared prefs
     */
    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_ID = "user_id"
        const val USER_PASSWORD = "user_password"
        const val KEEP_ME_IN = "keep_me_in"
    }


    fun saveAuthToken(token: String) {
        val editor = sharedPreferences.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun getAccessToken(): String? {
        return sharedPreferences.getString(USER_TOKEN, null)
    }

    /**
     * Refresh user token using old token or email&password
     * Return empty string token if there is no valid old token and email&password
     */
    fun getRefreshToken(): String {
        val value =
            sharedPreferences.getString(USER_ID, "") + ":" + sharedPreferences.getString(
                USER_PASSWORD,
                ""
            )
        val oldToken = getAccessToken()

        val token: String = if (oldToken != null && oldToken != "")
            sessionWebService.refreshToken(oldToken)
        else if (value == ":") {
            ""
        } else {
            sessionWebService.getToken(value)
        }
        val editor = sharedPreferences.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
        return token
    }

    fun logout() {
        val editor = sharedPreferences.edit()
        editor.putString(USER_TOKEN, "")
        editor.putString(USER_ID, "")
        editor.putString(USER_PASSWORD, "")
        editor.putBoolean(KEEP_ME_IN, false)
        editor.apply()
    }
}