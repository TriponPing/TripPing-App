package com.tripping.app.ui.screen

import androidx.compose.foundation.background
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
fun SignUpScreen(
    onSignUpClick: (nickname: String, email: String, password: String) -> Unit,
    onLoginLinkClick: () -> Unit
) {
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ===== 상단 배너 영역 =====
        // TODO: Image(painter = painterResource(R.drawable.배너이미지), ...) 로 교체
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(257.dp)
                .background(Color(0xFFE3F2F1))
        )

        // ===== 로고("Trip Ping") 자리 =====
        // TODO: 이미지 로고로 교체 예정이면 이 Box는 지워도 됨
        Box(
            modifier = Modifier
                .offset(x = 94.dp, y = 126.dp)
                .size(224.dp, 89.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Trip Ping", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFF3AAFA9))
        }

        // ===== "회원가입" 제목 =====
        Text(
            text = "회원가입",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 122.dp, y = 211.dp)
        )

        // ===== 서브 텍스트 =====
        Text(
            text = "TripPing과 함께 멋진 여행을 시작해요",
            fontSize = 13.sp,
            color = Color(0xFF818181),
            modifier = Modifier.offset(x = 103.dp, y = 271.dp)
        )

        // ===== 닉네임 라벨 =====
        Text(
            text = "닉네임",
            fontSize = 16.sp,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 44.dp, y = 320.dp)
        )

        // ===== 닉네임 입력창 =====
        OutlinedTextField(
            value = nickname,
            onValueChange = { nickname = it },
            modifier = Modifier
                .offset(x = 44.dp, y = 355.dp)
                .size(324.dp, 52.dp),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color(0xFFD0D0D0),
                focusedBorderColor = Color(0xFFD0D0D0)
            )
        )

        // ===== 이메일 라벨 =====
        Text(
            text = "이메일",
            fontSize = 16.sp,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 44.dp, y = 430.dp)
        )

        // ===== 이메일 입력창 =====
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier
                .offset(x = 44.dp, y = 461.dp)
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
            modifier = Modifier.offset(x = 44.dp, y = 536.dp)
        )

        // ===== 비밀번호 입력창 =====
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier
                .offset(x = 44.dp, y = 567.dp)
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

        // ===== 회원가입 버튼 =====
        Button(
            onClick = { onSignUpClick(nickname, email, password) },
            modifier = Modifier
                .offset(x = 44.dp, y = 703.dp)
                .size(324.dp, 66.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AA2D9))
        ) {
            Text(text = "회원가입", fontSize = 16.sp, color = Color(0xFFFFFFFF))
        }

        // ===== "이미 계정이 있나요? 로그인" =====
        Text(
            text = "이미 계정이 있나요? ",
            fontSize = 12.sp,
            color = Color(0xFF818181),
            modifier = Modifier.offset(x = 122.dp, y = 781.dp)
        )
        Text(
            text = "로그인",
            fontSize = 13.sp,
            color = Color(0xFF22567E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .offset(x = 232.dp, y = 781.dp)
                .clickable { onLoginLinkClick() }
        )
    }
}