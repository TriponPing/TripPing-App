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
import androidx.compose.ui.draw.alpha
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
    modifier: Modifier = Modifier, // 👈 네비게이션 바 패딩 등을 외부에서 받을 수 있도록 modifier 추가
    onAddPingClick: () -> Unit,
    onPingLogClick: (Int) -> Unit,
    onRouteCardClick: (Int) -> Unit
) {
    // 현재 진행 중인 여행의 핑 리스트 (테스트를 위해 빈 리스트로 설정)
    val mockPings = remember {
        emptyList<PingItem>()
        /*
        listOf(
            PingItem(1, "ABC카페", "12시 30분", PingStatus.DONE),
            PingItem(2, "XYZ박물관", "14시 00분", PingStatus.CURRENT),
            PingItem(3, "00식당", "18시 30분", PingStatus.UPCOMING)
        )
        */
    }

    val hasOngoingTrip = mockPings.isNotEmpty()
    val canAddMorePing = mockPings.size < 4 // 핑 개수가 4개 미만일 때만 true

    LazyColumn(
        modifier = modifier // 👈 전달받은 modifier를 적용하여 하단 네비게이션 바와 영역 조율
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Text(text = "여행을 기록해보아요!", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 조건 분기: 진행 중인 여행이 없을 때는 공룡 화면, 있을 때는 타임라인 및 핑 카드 표시
        if (!hasOngoingTrip) {
            item {
                EmptyTripView()
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            item {
                // 핑 개수가 4개 이상이어도 최대 4개까지만 타임라인에 표시
                TimelineDots(pings = mockPings.take(4))
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(mockPings) { ping ->
                PingCard(ping = ping, onLinkClick = { onPingLogClick(ping.id) })
                Spacer(modifier = Modifier.height(10.dp))
            }

            // 여행이 있고, 핑 개수가 4개 미만일 때만 + 버튼 노출
            if (canAddMorePing) {
                item {
                    AddPingButton(onClick = onAddPingClick)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        // 아래 배너와 추천 코스는 항상 같은 자리에 노출
        item {
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

// 🦕 진행 중인 여행이 없을 때 보여주는 빈 화면 컴포저블 (Row를 사용해 잘림 없이 나란히 배치)
@Composable
internal fun EmptyTripView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        // 공룡과 창문이 자연스럽게 들어가도록 Row 배치
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 공룡 이미지 (불투명도 20%)
            Image(
                painter = painterResource(id = R.drawable.trip_empty_dino),
                contentDescription = "공룡",
                modifier = Modifier
                    .width(120.dp)
                    .height(180.dp)
                    .alpha(0.20f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // 창문 이미지 (불투명도 15%)
            Image(
                painter = painterResource(id = R.drawable.trip_empty_window),
                contentDescription = "창문",
                modifier = Modifier
                    .width(150.dp)
                    .height(210.dp)
                    .alpha(0.15f)
            )
        }

        // "진행중인 여행이 없어요" 텍스트 (중앙 배치)
        Text(
            text = "진행중인 여행이 없어요",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF818181)
        )
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