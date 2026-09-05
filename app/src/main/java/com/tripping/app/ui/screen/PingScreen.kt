package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===== 색상 =====
private val BluePrimary = Color(0xFF4A72C4)

private enum class PingTab { RECORD, LOG }

@Composable
fun PingScreen(
    onAddPingClick: () -> Unit = {},
    onPingLogClick: (Long) -> Unit = {},
    onRouteCardClick: (Long) -> Unit = {}, // 👈 Int -> Long으로 수정 (tripId가 Long이라서)
    onCourseClick: (Long) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(PingTab.RECORD) }

    // containerColor 명시 안 하면 Material3 기본 배경(연한 보라)이 쓰여서, 리스트 내용이 짧을 때
    // 하단 네비바 위로 그 색이 띠처럼 삐져나와 보임 -> 흰색으로 고정
    Scaffold(containerColor = Color.White) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(text = "Ping", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TabToggle(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            }

            when (selectedTab) {
                // 기록 탭 컴포저블은 PingRecordContent.kt 걸 사용
                PingTab.RECORD -> PingRecordContent(
                    onAddPingClick = onAddPingClick,
                    onPingLogClick = onPingLogClick,
                    onRouteCardClick = onRouteCardClick
                )
                // 로그 탭 컴포저블은 PingLogContent.kt 걸 사용
                PingTab.LOG -> PingLogContent(
                    onCourseClick = onCourseClick
                )
            }
        }
    }
}

@Composable
private fun TabToggle(selectedTab: PingTab, onTabSelected: (PingTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TabItem(
            text = "기록",
            selected = selectedTab == PingTab.RECORD,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(PingTab.RECORD) }
        )
        TabItem(
            text = "로그",
            selected = selectedTab == PingTab.LOG,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(PingTab.LOG) }
        )
    }
}

@Composable
private fun TabItem(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) BluePrimary else Color(0xFFD9D9D9))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}