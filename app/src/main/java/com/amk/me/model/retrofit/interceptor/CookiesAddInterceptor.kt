package com.amk.me.model.retrofit.interceptor

import android.content.SharedPreferences
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject

class CookiesAddInterceptor @Inject constructor(private var sharedPreferences: SharedPreferences) : Interceptor {


    private val PREF_COOKIES = "PREF_COOKIES"

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestBuilder: Request.Builder = chain.request().newBuilder()

        val preferences =
            sharedPreferences.getStringSet(PREF_COOKIES, HashSet()) as HashSet<String>?

        for (cookie in preferences!!) {
            requestBuilder.addHeader("Cookie", cookie)
        }

        return chain.proceed(requestBuilder.build());
    }
}