package com.amk.me.model.retrofit

import retrofit2.http.GET
import retrofit2.http.Header

interface SessionWebService {

    /**
     * Get user token using email and password
     */
    @GET("user/token")
    fun getToken(@Header("Authorization") email_password: String): String

    /**
     * Get user token using the old token or email & password
     */
    @GET("user/token")
    fun refreshToken(@Header("Authorization") authorization: String): String

}