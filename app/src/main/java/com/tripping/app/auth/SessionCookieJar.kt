package com.tripping.app.auth

import android.content.Context
import android.content.SharedPreferences
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * 서버가 로그인 성공 시 내려주는 세션 쿠키를 저장하고,
 * 이후 모든 요청에 자동으로 실어 보내는 역할.
 * SharedPreferences에 저장하기 때문에 앱을 껐다 켜도 로그인 상태가 유지됨.
 */
class SessionCookieJar(context: Context) : CookieJar {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    private val cookieStore = mutableMapOf<String, List<Cookie>>()

    init {
        // 앱 시작 시 저장된 쿠키 불러오기
        val saved = prefs.getString(KEY_COOKIES, null)
        if (saved != null) {
            val host = prefs.getString(KEY_HOST, null)
            if (host != null) {
                val cookies = saved.split(SEPARATOR).mapNotNull { raw ->
                    Cookie.parse(HttpUrl.Builder().scheme("https").host(host).build(), raw)
                }
                cookieStore[host] = cookies
            }
        }
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        if (cookies.isEmpty()) return
        cookieStore[url.host] = cookies

        // 다음 실행에도 유지되도록 저장
        prefs.edit()
            .putString(KEY_HOST, url.host)
            .putString(KEY_COOKIES, cookies.joinToString(SEPARATOR) { it.toString() })
            .apply()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        return cookieStore[url.host] ?: emptyList()
    }

    /** 로그아웃 시 세션 쿠키 전부 삭제 */
    fun clear() {
        cookieStore.clear()
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_HOST = "session_host"
        private const val KEY_COOKIES = "session_cookies"
        private const val SEPARATOR = "||"
    }
}