package com.tripping.app.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// TODO: 실제 홈 화면 디자인 나오면 이 파일 내용 교체
// 하단 네비게이션 바는 AppNavigation.kt에서 공통으로 관리함 (여기서 직접 안 그림)
@Composable
fun HomeScreen(
    onGoToMyPageClick: () -> Unit = {},
    onNavigateToPlaceSearch: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "홈 화면",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "로그인 성공! 네비게이션 잘 연결됨 👍")

        Spacer(modifier = Modifier.height(24.dp))

        // TODO: 테스트용 임시 버튼, 실제 홈 화면 완성되면 삭제
        Button(onClick = onNavigateToPlaceSearch) {
            Text("관광지 검색 화면 테스트")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // TODO: 임시 테스트용 링크 - 마이페이지 아니어도 하단 네비 "마이" 탭으로 갈 수 있어서 나중에 지워도 됨
        Text(
            text = "마이페이지 보기 (테스트용)",
            color = Color(0xFF0074CE),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.clickable { onGoToMyPageClick() }
        )
    }
}