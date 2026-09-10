package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripping.app.R
import com.tripping.app.data.response.RegionResponse

// ===== 회원가입 - 2단계 (피그마 반영) =====
// 1단계: 닉네임/이메일/비밀번호 입력 -> "다음"
// 2단계: 사는 지역 선택(GET /regions) -> "회원가입" (실제 가입 요청은 여기서 나감)
@Composable
fun SignUpScreen(
    regions: List<RegionResponse>,
    onLoadRegions: () -> Unit,
    onSignUpClick: (nickname: String, email: String, password: String, regionId: String?) -> Unit,
    onLoginLinkClick: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var step by remember { mutableStateOf(1) } // 1 = 기본 정보, 2 = 지역 선택
    var nickname by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRegionId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) { onLoadRegions() }

    when (step) {
        1 -> SignUpStep1(
            nickname = nickname,
            onNicknameChange = { nickname = it },
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            onNextClick = { step = 2 },
            onLoginLinkClick = onLoginLinkClick
        )
        else -> SignUpStep2(
            regions = regions,
            selectedRegionId = selectedRegionId,
            onRegionSelected = { selectedRegionId = it },
            onSignUpClick = { onSignUpClick(nickname, email, password, selectedRegionId) },
            onLoginLinkClick = onLoginLinkClick,
            isLoading = isLoading,
            errorMessage = errorMessage
        )
    }
}

// ===== 1단계: 닉네임 / 이메일 / 비밀번호 =====
@Composable
private fun SignUpStep1(
    nickname: String,
    onNicknameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    onNextClick: () -> Unit,
    onLoginLinkClick: () -> Unit
) {
    val canProceed = nickname.isNotBlank() && email.isNotBlank() && password.isNotBlank()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ===== 상단 배너 영역 =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(257.dp)
                .background(Color(0xFFE3F2F1))
        )

        // ===== 로고 =====
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Trip Ping",
            modifier = Modifier
                .offset(x = 121.dp, y = 55.dp)
                .size(170.dp, 153.dp)
        )

        Text(
            text = "회원가입",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 122.dp, y = 211.dp)
        )

        Text(
            text = "TripPing과 함께 멋진 여행을 시작해요",
            fontSize = 13.sp,
            color = Color(0xFF818181),
            modifier = Modifier.offset(x = 103.dp, y = 271.dp)
        )

        Text(
            text = "닉네임",
            fontSize = 16.sp,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 44.dp, y = 320.dp)
        )
        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
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

        Text(
            text = "이메일",
            fontSize = 16.sp,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 44.dp, y = 430.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
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

        Text(
            text = "비밀번호",
            fontSize = 16.sp,
            color = Color(0xFF000000),
            modifier = Modifier.offset(x = 44.dp, y = 536.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
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

        Text(
            text = "회원가입까지 자기 사는 지역을 받아요",
            fontSize = 11.sp,
            color = Color(0xFF818181),
            modifier = Modifier.offset(x = 44.dp, y = 638.dp)
        )

        // ===== "다음" 버튼 =====
        Button(
            onClick = onNextClick,
            enabled = canProceed,
            modifier = Modifier
                .offset(x = 44.dp, y = 668.dp)
                .size(324.dp, 66.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AA2D9))
        ) {
            Text(text = "다음", fontSize = 16.sp, color = Color(0xFFFFFFFF))
        }

        Text(
            text = "이미 계정이 있나요? ",
            fontSize = 12.sp,
            color = Color(0xFF818181),
            modifier = Modifier.offset(x = 122.dp, y = 746.dp)
        )
        Text(
            text = "로그인",
            fontSize = 13.sp,
            color = Color(0xFF22567E),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .offset(x = 232.dp, y = 746.dp)
                .clickable { onLoginLinkClick() }
        )
    }
}

// ===== 2단계: 사는 지역 선택 =====
@Composable
private fun SignUpStep2(
    regions: List<RegionResponse>,
    selectedRegionId: String?,
    onRegionSelected: (String) -> Unit,
    onSignUpClick: () -> Unit,
    onLoginLinkClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(257.dp)
                .background(Color(0xFFE3F2F1))
        )

        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Trip Ping",
            modifier = Modifier
                .offset(x = 121.dp, y = 55.dp)
                .size(170.dp, 153.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 211.dp, start = 44.dp, end = 44.dp)
        ) {
            Text(text = "회원가입", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = Color(0xFF000000))
            Spacer(modifier = Modifier.height(14.dp))
            Text(text = "사는 지역", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF000000))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "어느 지역에 거주하고 계신가요?", fontSize = 12.sp, color = Color(0xFF818181))
            Spacer(modifier = Modifier.height(16.dp))

            if (regions.isEmpty()) {
                Text(text = "지역 목록을 불러오는 중...", fontSize = 12.sp, color = Color(0xFF818181))
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(regions) { region ->
                        RegionChip(
                            label = region.regionName,
                            selected = region.regionId == selectedRegionId,
                            onClick = { onRegionSelected(region.regionId) }
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, fontSize = 12.sp, color = Color(0xFFE74C3C))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onSignUpClick,
                enabled = !isLoading && selectedRegionId != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AA2D9))
            ) {
                Text(text = if (isLoading) "가입 중..." else "회원가입", fontSize = 16.sp, color = Color(0xFFFFFFFF))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "이미 계정이 있나요? ", fontSize = 12.sp, color = Color(0xFF818181))
                Text(
                    text = "로그인",
                    fontSize = 13.sp,
                    color = Color(0xFF22567E),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLoginLinkClick() }
                )
            }
        }
    }
}

@Composable
private fun RegionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Color(0xFF5AA2D9) else Color.White)
            .border(
                width = 1.dp,
                color = if (selected) Color(0xFF5AA2D9) else Color(0xFFD0D0D0),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) Color.White else Color(0xFF3D3D3D)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    SignUpScreen(
        regions = emptyList(),
        onLoadRegions = {},
        onSignUpClick = { _, _, _, _ -> },
        onLoginLinkClick = {}
    )
}
