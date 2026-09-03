plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.tripping.app"
    compileSdk {
        version = release(37) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.tripping.app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // ===== 기본 Compose 관련 라이브러리 (프로젝트 생성 시 자동 포함) =====
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // ===== Retrofit - 서버(백엔드)와 통신할 때 사용 =====
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0") // 서버 응답(JSON) <-> Kotlin 객체 자동 변환
    implementation("com.squareup.retrofit2:converter-scalars:2.11.0") // 회원가입 등 순수 텍스트(String) 응답 처리용

    // ===== OkHttp - Retrofit 내부에서 실제 네트워크 통신을 담당 =====
    // CookieJar(세션 쿠키 관리)를 위해서도 필요함 (백엔드가 세션 기반 로그인이라 필수)
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0") // 통신 로그를 콘솔에 찍어줌 (디버깅용, 필수는 아니지만 있으면 편함)

    // ===== ViewModel + Coroutine - 화면 상태 관리 및 비동기 처리에 사용 =====
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7") // viewModelScope 등 Compose에서 ViewModel 쓸 때 필요
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0") // suspend 함수(비동기 통신) 실행에 필요

    // ===== Navigation - 화면 간 이동(로그인 -> 회원가입 -> 홈 등)을 관리 =====
    // 이게 있으면 MainActivity를 계속 안 건드리고, 새 화면을 자유롭게 추가/연결할 수 있음
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ===== 네이버 지도 SDK - "나의 여행 지도" 화면에서 사용 =====
    implementation("com.naver.maps:map-sdk:3.23.3")
}