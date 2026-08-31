package com.tripping.app.ui.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    onLoginClick: (email: String, password: String) -> Unit,
    onSignUpLinkClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ===== 로고 배너 자리 =====
        // TODO: Image(painter = painterResource(R.drawable.배너이미지), ...) 로 교체
        Box(
            modifier = Modifier
                .offset(x = 94.dp, y = 126.dp)
                .size(224.dp, 89.dp)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Text("Trip Ping", fontSize = 20.sp, color = Color(0xFFBBBBBB))
        }

        // ===== 언어 선택 (우측 상단) =====
        Box(
            modifier = Modifier
                .offset(x = 286.dp, y = 34.dp)
                .size(107.dp, 31.dp)
                .border(1.dp, Color(0xFFD0D0D0), RoundedCornerShape(15.5.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("한국어", fontSize = 13.sp, color = Color(0xFF818181))
        }

        // ===== "로그인" 제목 =====
        Text(
            text = "로그인",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 122.dp, y = 215.dp)
        )

        // ===== 서브 텍스트 =====
        Text(
            text = "TripPing에 오신 것을 환영해요!",
            fontSize = 13.sp,
            color = Color(0xFF818181),
            modifier = Modifier.offset(x = 98.dp, y = 276.dp)
        )

        // ===== 이메일 라벨 =====
        Text(
            text = "이메일",
            fontSize = 16.sp,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 49.dp, y = 351.dp)
        )

        // ===== 이메일 입력창 =====
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .offset(x = 49.dp, y = 386.dp)
                .size(324.dp, 52.dp),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFD0D0D0),
                focusedBorderColor = Color(0xFFD0D0D0)
            )
        )

        // ===== 비밀번호 라벨 =====
        Text(
            text = "비밀번호",
            fontSize = 16.sp,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 49.dp, y = 448.dp)
        )

        // ===== 비밀번호 입력창 =====
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .offset(x = 49.dp, y = 483.dp)
                .size(324.dp, 52.dp),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFD0D0D0),
                focusedBorderColor = Color(0xFFD0D0D0)
            )
        )

        // ===== 로그인 버튼 =====
        Button(
            onClick = { onLoginClick(email, password) },
            modifier = Modifier
                .offset(x = 49.dp, y = 578.dp)
                .size(324.dp, 66.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AA2D9))
        ) {
            Text(text = "로그인", fontSize = 16.sp, color = Color(0xFFFFFFFF))
        }

        // ===== 회원가입 링크 =====
        Text(
            text = "회원가입",
            fontSize = 13.sp,
            color = Color(0xFF22567E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .offset(x = 179.dp, y = 661.dp)
                .clickable { onSignUpLinkClick() }
        )
    }
}