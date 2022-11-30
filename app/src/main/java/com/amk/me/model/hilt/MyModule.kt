package com.amk.me.model.hilt

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.amk.me.model.MySharedPreferences
import com.amk.me.model.retrofit.SessionManager
import com.amk.me.model.retrofit.SessionWebService
import com.amk.me.model.retrofit.UserWebService
import com.amk.me.model.retrofit.interceptor.AuthInterceptor
import com.amk.me.model.retrofit.interceptor.CacheInterceptor
import com.amk.me.model.retrofit.interceptor.CookiesAddInterceptor
import com.amk.me.model.retrofit.interceptor.CookiesReceiveInterceptor
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.File
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MyModule {

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SessionRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class CommonRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BaseUrl

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class CachePath

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class SharePrefsFile


    companion object {
        @BaseUrl
        @Singleton
        @Provides
        fun getBaseUrl(): String {
            return "http://172.16.82.2:8080"
        }

        @CachePath
        @Singleton
        @Provides
        fun getPathToCacheFile(@ApplicationContext context: Context): String {
            val mainDirFile: File = context.filesDir
            val file = File(mainDirFile.path, "cf")
            return file.absolutePath
        }

        @SharePrefsFile
        @Singleton
        @Provides
        fun getPathToSharedPrefs(): String {
            return "sp"
        }

        @Singleton
        @Provides
        fun getSharedPreferences(
            @ApplicationContext context: Context,
            @SharePrefsFile filePath: String
        ): SharedPreferences {
            Log.e("TAG", "getSharedPreferences: $filePath")

            var sharedPreferences: SharedPreferences? = null
            while (sharedPreferences == null) {
                Log.e("TAG", "sharedPreferences == null")

                sharedPreferences =
                    MySharedPreferences().getEncryptedSharedPreferences(context, filePath)
            }
            return sharedPreferences
        }

        @SessionRetrofit
        @Singleton
        @Provides
        fun provideOkHttpSessionClient(
            cookiesReceiveInterceptor: CookiesReceiveInterceptor,
            cookiesAddInterceptor: CookiesAddInterceptor,
            cacheInterceptor: CacheInterceptor
        ): OkHttpClient {

            val clientBuilder = OkHttpClient.Builder()
                .addInterceptor(cookiesReceiveInterceptor)
                .addInterceptor(cookiesAddInterceptor)
                .addInterceptor(cacheInterceptor)

            return clientBuilder.build()
        }

        @SessionRetrofit
        @Singleton
        @Provides
        fun provideSessionRetrofit(
            @BaseUrl baseUrl: String,
            @SessionRetrofit sessionClient: OkHttpClient
        ): Retrofit {
            val gson = GsonBuilder()
                .setLenient()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create()
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(sessionClient)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }

        @SessionRetrofit
        @Singleton
        @Provides
        fun sessionWebService(@SessionRetrofit retrofit: Retrofit): SessionWebService {
            return retrofit.create(SessionWebService::class.java)

        }

        @Singleton
        @Provides
        fun getSessionManager(
            sharedPreferences: SharedPreferences,
            @SessionRetrofit sessionWebService: SessionWebService
        ): SessionManager {
            return SessionManager(sharedPreferences, sessionWebService)
        }

        @CommonRetrofit
        @Singleton
        @Provides
        fun provideOkHttpClient(
            authInt: AuthInterceptor,
            cookiesReceiveInterceptor: CookiesReceiveInterceptor,
            cookiesAddInterceptor: CookiesAddInterceptor,
            cacheInterceptor: CacheInterceptor
        ): OkHttpClient {

            val clientBuilder = OkHttpClient.Builder()
                .addInterceptor(authInt)
                .addInterceptor(cookiesReceiveInterceptor)
                .addInterceptor(cookiesAddInterceptor)
                .addInterceptor(cacheInterceptor)

            return clientBuilder.build()
        }

        @CommonRetrofit
        @Singleton
        @Provides
        fun retrofit(
            @BaseUrl baseUrl: String,
            @CommonRetrofit okHttpClient: OkHttpClient
        ): Retrofit {

            val gson = GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .setLenient()
                .create()
            return Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
//                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

        }

        @Singleton
        @Provides
        fun userWebservice(@CommonRetrofit retrofit: Retrofit): UserWebService {
            return retrofit.create(UserWebService::class.java)
        }
    }

}