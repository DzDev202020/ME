package com.amk.me.model.retrofit.interceptor

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class CookiesReceiveInterceptor @Inject constructor(private var sharedPreferences: SharedPreferences) :
    Interceptor {

    private val PREF_COOKIES = "PREF_COOKIES"


    override fun intercept(chain: Interceptor.Chain): Response {
        val originalResponse = chain.proceed(chain.request())

        if (originalResponse.headers("Set-Cookie").isNotEmpty()) {
            val cookies = sharedPreferences
                .getStringSet(PREF_COOKIES, HashSet()) as HashSet<String>?
            for (header in originalResponse.headers("Set-Cookie")) {
                cookies!!.add(header)
            }
            val editor = sharedPreferences.edit()
            editor.putStringSet(PREF_COOKIES, cookies).apply()
            editor.apply()
        }

        return originalResponse
    }
}