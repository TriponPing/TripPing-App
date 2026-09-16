package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.tripping.app.R
import com.tripping.app.data.response.RegionResponse

// 👈 새로 추가: 원스토어 심사/개인정보보호법 대응 - 회원가입 시 필수 동의 절차가 아예 없던 문제.
// 별도 웹페이지를 새로 만들고 배포하는 대신, 앱 안에서 다이얼로그로 바로 보여줘서 오늘 안에
// 반영 가능하게 함.
private const val PRIVACY_POLICY_TEXT = """TripPing 개인정보처리방침

TripPing(이하 "회사")은 이용자의 개인정보를 소중히 다루며, 아래와 같이 개인정보를 수집·이용합니다.

1. 수집하는 개인정보 항목
- 필수: 닉네임, 이메일, 비밀번호(암호화 저장), 거주 지역
- 선택: 프로필 사진
- 서비스 이용 중 생성: 여행(핑) 기록의 위치 좌표, 방문 장소, 후기 사진 및 텍스트

2. 개인정보 수집·이용 목적
- 회원 가입 및 본인 확인, 로그인 유지
- 여행 경로 기록, 장소 추천, 인기 장소/루트 통계 제공 등 서비스 제공
- 문의 응대

3. 개인정보 보유 및 이용 기간
- 회원 탈퇴 시 지체 없이 파기합니다. 단, 관계 법령에 따라 보존이 필요한 정보는 해당 기간 동안 보관합니다.

4. 개인정보의 제3자 제공
- 회사는 이용자의 개인정보를 외부에 제공하지 않습니다. 다만 법령에 근거가 있거나 이용자가 별도로 동의한 경우는 예외로 합니다.

5. 이용자의 권리
- 이용자는 언제든지 자신의 개인정보를 조회·수정하거나 회원 탈퇴(삭제)를 요청할 수 있습니다.

6. 문의처
- 이메일: hknmttpage@gmail.com

본 방침은 서비스 개선에 따라 변경될 수 있으며, 변경 시 앱 내 공지를 통해 안내합니다.
"""

// ===== 회원가입 - 2단계 (피그마 반영) =====
// 1단계: 닉네임/이메일/비밀번호 입력 -> "다음"
// 2단계: 사는 지역 선택(GET /regions) -> "회원가입" (실제 가입 요청은 여기서 나감)
// 절대좌표(offset) 대신 Column + fillMaxWidth로 짜서 기기 화면 너비가 달라도 항상 같은 비율로 보이게 함.
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

// 회원가입 두 단계 화면 공통 상단부 - 배경 일러스트 위에 로고를 가운데로 얹음
@Composable
private fun SignUpBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            // 고정 height + Crop을 쓰면 이미지 아래쪽의 자연스러운 그라데이션 부분이 잘려서
            // 경계선이 뚝 끊겨 보임 - 원본 이미지 비율(412:271) 그대로 보여줘서 안 잘리게 함
            .aspectRatio(412f / 271f)
    ) {
        Image(
            painter = painterResource(id = R.drawable.image33),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize()
        )
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Trip Ping",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
                .fillMaxWidth(0.45f)
                .aspectRatio(355f / 125f) // logo.png를 여백 없이 꽉 채워 크롭해둔 실제 비율
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()) // 키보드가 올라오거나 화면이 작을 때 버튼이 가려지지 않도록 스크롤 가능하게 함
    ) {
        SignUpBanner()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 44.dp)
                .offset(y = (-26).dp) // "회원가입" 제목이 배경 이미지 아래쪽과 살짝 겹치도록 위로 당김
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "회원가입",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "TripPing과 함께 멋진 여행을 시작해요",
                fontSize = 13.sp,
                color = Color(0xFF818181),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(28.dp))

            Text(text = "닉네임", fontSize = 16.sp, color = Color(0xFF000000))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = nickname,
                onValueChange = onNicknameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color(0xFFD0D0D0),
                    focusedBorderColor = Color(0xFFD0D0D0)
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "이메일", fontSize = 16.sp, color = Color(0xFF000000))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
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
            Text(text = "비밀번호", fontSize = 16.sp, color = Color(0xFF000000))
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
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

            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "회원가입까지 자기 사는 지역을 받아요",
                fontSize = 11.sp,
                color = Color(0xFF818181)
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNextClick,
                enabled = canProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(66.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AA2D9))
            ) {
                Text(text = "다음", fontSize = 16.sp, color = Color(0xFFFFFFFF))
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
    var privacyAgreed by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        SignUpBanner()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 44.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "회원가입",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF000000),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
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

            Spacer(modifier = Modifier.height(12.dp))

            // 👈 새로 추가: 개인정보 수집·이용 동의 (필수) - 체크 안 하면 회원가입 버튼 비활성화
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = privacyAgreed,
                    onCheckedChange = { privacyAgreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFF5AA2D9))
                )
                Text(
                    text = "[필수] 개인정보 수집·이용에 동의합니다",
                    fontSize = 13.sp,
                    color = Color(0xFF3D3D3D),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "보기",
                    fontSize = 13.sp,
                    color = Color(0xFF22567E),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { showPrivacyDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onSignUpClick,
                enabled = !isLoading && selectedRegionId != null && privacyAgreed,
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

    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
}

// 개인정보처리방침 전문 - 회원가입 화면의 "보기"와 설정 화면 양쪽에서 재사용
@Composable
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Text(text = "개인정보처리방침", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = PRIVACY_POLICY_TEXT,
                fontSize = 13.sp,
                color = Color(0xFF3D3D3D),
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5AA2D9))
            ) {
                Text(text = "확인", color = Color.White)
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
