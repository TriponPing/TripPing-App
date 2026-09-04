package com.tripping.app.data.api

import android.content.Context
import com.tripping.app.auth.SessionCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private lateinit var cookieJar: SessionCookieJar

    fun init(context: Context) {
        if (!::cookieJar.isInitialized) {
            cookieJar = SessionCookieJar(context)
        }
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .build()
    }

    val authApi: AuthApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AuthApi::class.java)
    }

    val placeApi: PlaceApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(PlaceApi::class.java)
    }

    fun clearSession() {
        if (::cookieJar.isInitialized) {
            cookieJar.clear()
        }
    }

    val routeApi: RouteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RouteApi::class.java)
    }
}