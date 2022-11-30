package com.amk.me.model.retrofit

import com.amk.me.model.models.User
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*


interface UserWebService {


    @GET("user")
    suspend fun readUser(@Header("Authorization") email_password: String): User?


    @POST("user")
    suspend fun updateUser(@Body user: User): User?

    @Multipart
    @POST("user/picture/{userId}")
    suspend fun uploadUserPicture(
        @Path("userId") userId: String,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @GET("user/picture/{userId}")
    suspend fun downloadUserPicture(@Path("userId") userId: String): Response<ResponseBody>

    @DELETE("user/picture/{userId}")
    suspend fun deleteUserPicture(@Path("userId") userId: String): Response<ResponseBody>

}