package com.tripping.app.data.api

import android.content.Context
import com.tripping.app.auth.SessionCookieJar
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

object RetrofitClient {

    private const val BASE_URL = "http://10.0.2.2:8080/" // 에뮬레이터 기준, 실기기는 PC IP로 변경

    private lateinit var cookieJar: SessionCookieJar

    /** 앱 시작 시(MainActivity onCreate) 딱 한 번 호출해서 Context를 넘겨줘야 함 */
    fun init(context: Context) {
        if (!::cookieJar.isInitialized) {
            cookieJar = SessionCookieJar(context)
        }
    }

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // 요청/응답 전체를 Logcat에 찍음 (디버깅용)
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            // 순서 중요: String처럼 단순 타입은 Scalars가 먼저 처리(순수 텍스트 그대로 반환),
            // 그 외 데이터 클래스는 Gson이 JSON으로 파싱함
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authApi: AuthApi by lazy { retrofit.create(AuthApi::class.java) }

    val myPageApi: MyPageApi by lazy { retrofit.create(MyPageApi::class.java) }

    /** 로그아웃 시 세션 쿠키 삭제 */
    fun clearSession() {
        if (::cookieJar.isInitialized) {
            cookieJar.clear()
        }
    }
}