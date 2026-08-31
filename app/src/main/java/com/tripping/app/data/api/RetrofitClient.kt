package com.tripping.app.data.api

import android.content.Context
import com.tripping.app.auth.SessionCookieJar
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/" // 에뮬레이터 기준, 실기기는 PC IP로 변경

    private lateinit var cookieJar: SessionCookieJar

    /** 앱 시작 시(MainActivity onCreate) 딱 한 번 호출해서 Context를 넘겨줘야 함 */
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

    /** 로그아웃 시 세션 쿠키 삭제 */
    fun clearSession() {
        if (::cookieJar.isInitialized) {
            cookieJar.clear()
        }
    }
}