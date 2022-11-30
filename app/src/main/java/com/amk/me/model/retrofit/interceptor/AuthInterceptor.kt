package com.amk.me.model.retrofit.interceptor

import com.amk.me.model.retrofit.SessionManager
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.net.HttpURLConnection
import javax.inject.Inject

class AuthInterceptor @Inject constructor(private val sessionManager: SessionManager) :
    Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.headers["Authorization"] != null && request.headers["Authorization"] != "") {
            val response = chain.proceed(request)
            if (response.code != HttpURLConnection.HTTP_UNAUTHORIZED)
                return response
        }

        val accessToken = sessionManager.getAccessToken()

        val response = chain.proceed(newRequestWithAccessToken(accessToken, request))

        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            var newAccessToken = sessionManager.getAccessToken()
            if (newAccessToken != accessToken) {
                return chain.proceed(newRequestWithAccessToken(newAccessToken, request))
            } else {

                newAccessToken = refreshToken()
                if (newAccessToken.isBlank()) {
                    return response
                }
                return chain.proceed(newRequestWithAccessToken(newAccessToken, request))
            }
        } else {
            val token = response.headers["Authorization"]
            val oldAccessToken = sessionManager.getAccessToken()
            if (token != null && token != oldAccessToken) {
                sessionManager.saveAuthToken(token)
            }
            return response
        }

    }


    private fun newRequestWithAccessToken(accessToken: String?, request: Request): Request =
        request.newBuilder().addHeader("Authorization", "Bearer $accessToken").build()


    private fun refreshToken(): String {
        synchronized(this) {
            return sessionManager.getRefreshToken()
        }
    }
}