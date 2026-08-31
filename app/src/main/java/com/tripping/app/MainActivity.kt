package com.tripping.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.tripping.app.navigation.AppNavigation
import com.tripping.app.ui.theme.TrippingTheme
import com.tripping.app.data.api.RetrofitClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        RetrofitClient.init(applicationContext)
        // ===== 앱 시작 시 초기화 코드 모음 =====
        // 다른 기능 추가할 때 여기에 초기화 코드 추가하면 됨 (예: 알림 초기화, 분석 툴 초기화 등)

        // 세션 쿠키 저장소(RetrofitClient) 초기화
        // - 로그인 시 서버가 내려주는 세션 쿠키를 SharedPreferences에 저장하기 위해 Context가 필요함
        // - 그래서 앱이 켜질 때 딱 한 번, 여기서 Context를 넘겨줘야 함
        // - 이 줄 없으면 로그인 API 호출 시 크래시 남 (auth/SessionCookieJar.kt 참고)
        RetrofitClient.init(applicationContext)

        // 새 초기화 코드는 이 아래에 추가하세요~~~~~~!



        enableEdgeToEdge()
        setContent {
            TrippingTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppNavigation()
                }
            }
        }
    }
}