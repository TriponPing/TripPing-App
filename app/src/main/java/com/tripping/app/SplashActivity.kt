package com.tripping.app

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.ComponentActivity

// Theme.Tripping이 AppCompat 계열이 아니라서(android:Theme.Material 기반) AppCompatActivity를 쓰면
// "You need to use a Theme.AppCompat theme" IllegalStateException으로 바로 크래시남.
// MainActivity와 동일하게 ComponentActivity를 사용함.
class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val ivDino = findViewById<ImageView>(R.id.ivDinoSplash)

        Handler(Looper.getMainLooper()).postDelayed({
            ivDino.setImageResource(R.drawable.splash2)
        }, 500)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }
}