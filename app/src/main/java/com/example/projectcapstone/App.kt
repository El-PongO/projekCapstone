package com.example.projectcapstone

import android.app.Application
import android.util.Log
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class App : Application() {
    companion object {
        private const val TAG = "IntegraAPI"

        // Debug hanya mencatat alamat, method, status HTTP, dan jenis error.
        // Password, JWT, dan body request TIDAK dicetak ke Logcat.
        private val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request()
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "REQUEST ${request.method} ${request.url}")
                }
                try {
                    val response = chain.proceed(request)
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "RESPONSE HTTP ${response.code} ${request.url.encodedPath}")
                    }
                    response
                } catch (e: IOException) {
                    if (BuildConfig.DEBUG) {
                        Log.e(TAG, "NETWORK ERROR ${e.javaClass.simpleName}: ${e.message}", e)
                    }
                    throw e
                }
            }
            .build()

        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .also {
                if (BuildConfig.DEBUG) Log.d(TAG, "ACTIVE BASE URL = ${BuildConfig.API_BASE_URL}")
            }

        val retrofitService = retrofit.create(WebService::class.java)
    }
}
