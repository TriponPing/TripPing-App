package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
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
import com.tripping.app.ui.model.PingItem
import com.tripping.app.ui.model.PingStatus

// ===== 색상 설정 =====
private val BluePrimary = Color(0xFF4A72C4)
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)
private val CardBorder = Color(0xFFECECEC)

@Composable
internal fun PingRecordContent(
    onAddPingClick: () -> Unit,
    onPingLogClick: (Int) -> Unit,
    onRouteCardClick: (Int) -> Unit
) {
    // 현재 진행 중인 여행의 핑 리스트
    val mockPings = remember {
        listOf(
            PingItem(1, "ABC카페", "12시 30분", PingStatus.DONE),
            PingItem(2, "XYZ박물관", "14시 00분", PingStatus.CURRENT),
            PingItem(3, "00식당", "18시 30분", PingStatus.UPCOMING)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Text(text = "여행을 기록해보아요!", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            // 파란 핑이 mockPings 개수만큼 자동으로 그려짐
            TimelineDots(pings = mockPings)
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(mockPings) { ping ->
            PingCard(ping = ping, onLinkClick = { onPingLogClick(ping.id) })
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
                Text(text = "기록을 추가하고 싶은 핑로그가 있나요?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "바로가기", fontSize = 13.sp, color = GrayText, modifier = Modifier.clickable { /* 바로가기 액션 */ })
            }
            Spacer(modifier = Modifier.height(12.dp))

            RouteCard(
                title = "쌍문 코스",
                dateText = "2026년 8월 13일 마지막 수정",
                pingCount = 4,
                onClick = { onRouteCardClick(1) }
            )
        }
    }
}

@Composable
internal fun TimelineDots(pings: List<PingItem>) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
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
internal fun PingCard(ping: PingItem, onLinkClick: () -> Unit) {
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = ping.placeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "···", fontSize = 14.sp, color = GrayText, fontWeight = FontWeight.Bold)
            }

            Text(text = ping.time, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "장소와 시간이 맞나요?", fontSize = 12.sp, color = GrayText, modifier = Modifier.weight(1f))
                Text(
                    text = "핑로그 기록/수정 하러가기",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.clickable { onLinkClick() }
                )
                Text(text = "›", fontSize = 14.sp, color = GrayText, modifier = Modifier.padding(start = 2.dp))
            }
        }
    }
}

@Composable
internal fun AddPingButton(onClick: () -> Unit) {
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
            contentDescription = "새로운 핑 추가",
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
internal fun HintBanner() {
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
        Text(text = "×", fontSize = 13.sp, color = GrayText, modifier = Modifier.clickable { /* 배너 닫기 */ })
    }
}

@Composable
internal fun RouteCard(title: String, dateText: String, pingCount: Int, onClick: () -> Unit) {
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
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = dateText, fontSize = 11.sp, color = GrayText)
        }
        Text(text = "핑 ${pingCount}개", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary)
    }
}

// 모든 상태에서 파란색 핑(blueping) → TimelineDots에서 pings 개수만큼 자동으로 그려짐
internal fun pingDrawable(status: PingStatus): Int = R.drawable.blueping