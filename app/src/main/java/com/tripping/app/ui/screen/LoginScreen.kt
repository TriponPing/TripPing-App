package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripping.app.R

// 절대좌표(offset) 대신 Column + fillMaxWidth로 짜서 기기 화면 너비가 달라도 항상 같은 비율로 보이게 함.
@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onSignUpLinkClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 44.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // ===== 언어 선택 (우측 상단) =====
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color(0xFFD0D0D0), RoundedCornerShape(15.5.dp))
                    .padding(horizontal = 16.dp, vertical = 7.dp)
            ) {
                Text("한국어", fontSize = 13.sp, color = Color(0xFF818181))
            }
        }

        Spacer(modifier = Modifier.height(36.dp))

        // ===== 로고 =====
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Trip Ping",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .fillMaxWidth(0.5f)
                .aspectRatio(355f / 125f) // logo.png를 여백 없이 꽉 채워 크롭해둔 실제 비율
        )

        Spacer(modifier = Modifier.height(28.dp))

        // ===== "로그인" 제목 =====
        Text(
            text = "로그인",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ===== 서브 텍스트 =====
        Text(
            text = "TripPing에 오신 것을 환영해요!",
            fontSize = 13.sp,
            color = Color(0xFF818181),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // ===== 이메일 =====
        Text(text = "이메일", fontSize = 16.sp, color = Color(0xFF000000))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFD0D0D0),
                focusedBorderColor = Color(0xFFD0D0D0)
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 비밀번호 =====
        Text(text = "비밀번호", fontSize = 16.sp, color = Color(0xFF000000))
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFD0D0D0),
                focusedBorderColor = Color(0xFFD0D0D0)
            )
        )

        // ===== 에러 메시지 =====
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = errorMessage, fontSize = 12.sp, color = Color(0xFFE74C3C))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ===== 로그인 버튼 =====
        Button(
            onClick = { onLoginClick(email, password) },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(66.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AA2D9))
        ) {
            Text(text = if (isLoading) "로그인 중..." else "로그인", fontSize = 16.sp, color = Color(0xFFFFFFFF))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 회원가입 링크 =====
        Text(
            text = "회원가입",
            fontSize = 13.sp,
            color = Color(0xFF22567E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .clickable { onSignUpLinkClick() }
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}
