package com.amk.me.model.retrofit.interceptor

import com.amk.me.model.hilt.MyModule
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.Interceptor
import okhttp3.Response
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CacheInterceptor @Inject constructor(@MyModule.CachePath pathToCacheFile: String) : Interceptor {


    override fun intercept(chain: Interceptor.Chain): Response {
        val response: Response = chain.proceed(chain.request())
        val cacheControl: CacheControl = CacheControl.Builder()
            .maxAge(1, TimeUnit.MINUTES) // 1 minutes cache
            .build()
        return response.newBuilder()
            .removeHeader("Pragma")
            .removeHeader("Cache-Control")
            .header("Cache-Control", cacheControl.toString())
            .addHeader("Cache-Control", "no-store")
            .addHeader("Cache-Control", "no-cache")
            .build()
    }

    var cacheFilePath: String = pathToCacheFile

    fun getCache(): Cache {
        return Cache(File(cacheFilePath), 10 * 1024 * 1024);
    }
}