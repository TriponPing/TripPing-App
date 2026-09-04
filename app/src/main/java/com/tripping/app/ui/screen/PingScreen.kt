package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripping.app.R

// ===== 색상 =====
private val BluePrimary = Color(0xFF4A72C4)
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)
private val CardBorder = Color(0xFFECECEC)

// ===== 화면에서 쓸 상태 모델 (지금은 목업 데이터) =====
data class PingItem(
    val id: Int,
    val placeName: String,
    val time: String,
    val status: PingStatus
)

enum class PingStatus { DONE, CURRENT, UPCOMING }

@Composable
fun PingScreen(
    onAddPingClick: () -> Unit = {},
    onPingLogClick: (Int) -> Unit = {},
    onRouteCardClick: (Int) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(PingTab.RECORD) }

    // TODO: ViewModel에서 PingApi.getOngoingPings(routeId) 호출 결과로 교체
    val mockPings = remember {
        listOf(
            PingItem(1, "ABC카페", "12시 30분", PingStatus.DONE),
            PingItem(2, "ABC카페", "12시 30분", PingStatus.CURRENT),
            PingItem(3, "ABC카페", "12시 30분", PingStatus.UPCOMING)
        )
    }

    Scaffold(
        topBar = { PingTopBar() }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Ping",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                TabToggle(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "여행을 기록해보아요!",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 타임라인 점 + 선
            item {
                TimelineDots(pings = mockPings)
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 핑 카드 리스트
            items(mockPings) { ping ->
                PingCard(
                    ping = ping,
                    onLinkClick = { onPingLogClick(ping.id) }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            item {
                AddPingButton(onClick = onAddPingClick)
                Spacer(modifier = Modifier.height(12.dp))

                HintBanner()
                Spacer(modifier = Modifier.height(28.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "기록을 추가하고 싶은 핑로그가 있나요?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "바로가기",
                        fontSize = 13.sp,
                        color = GrayText
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                // TODO: 최근 루트 목록 API 연동 시 리스트로 교체
                RouteCard(
                    title = "쌍문 코스",
                    dateText = "2026년 8월 13일 마지막 수정",
                    pingCount = 4,
                    onClick = { onRouteCardClick(1) }
                )
            }
        }
    }
}

private enum class PingTab { RECORD, LOG }

@Composable
private fun PingTopBar() {
    Text(
        text = "Ping 기록",
        fontSize = 13.sp,
        color = GrayText,
        modifier = Modifier.padding(start = 20.dp, top = 12.dp)
    )
}

@Composable
private fun TabToggle(selectedTab: PingTab, onTabSelected: (PingTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(GrayBg, RoundedCornerShape(22.dp))
            .padding(4.dp)
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
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) BluePrimary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else GrayText,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )
    }
}

// status에 맞는 핑 이미지 리소스 반환
private fun pingDrawable(status: PingStatus): Int = when (status) {
    PingStatus.DONE -> R.drawable.blueping
    PingStatus.CURRENT -> R.drawable.greenping
    PingStatus.UPCOMING -> R.drawable.blueping
}

@Composable
private fun TimelineDots(pings: List<PingItem>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        pings.forEachIndexed { index, ping ->
            Image(
                painter = painterResource(id = pingDrawable(ping.status)),
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            if (index != pings.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                        .background(BluePrimary)
                )
            }
        }
    }
}

@Composable
private fun PingCard(ping: PingItem, onLinkClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = pingDrawable(ping.status)),
            contentDescription = null,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = ping.placeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = ping.time, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "장소와 시간이 맞나요?",
                    fontSize = 12.sp,
                    color = GrayText,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "핑로그 기록/수정 하러가기",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.clickable { onLinkClick() }
                )
                Text(
                    text = "›",
                    fontSize = 14.sp,
                    color = GrayText,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun AddPingButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.plus_circle),
            contentDescription = "핑 추가",
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun HintBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrayBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "핑을 꾹 눌러 순서를 변경할 수 있어요   잊은 핑을 추가해보세요",
            fontSize = 11.sp,
            color = GrayText,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "×",
            fontSize = 13.sp,
            color = GrayText
        )
    }
}

@Composable
private fun RouteCard(title: String, dateText: String, pingCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GrayBg)
            // TODO: 썸네일 이미지 있으면 Image(...)로 교체
        )
        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = dateText, fontSize = 11.sp, color = GrayText)
        }

        Text(
            text = "핑 ${pingCount}개",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = BluePrimary
        )
    }
}