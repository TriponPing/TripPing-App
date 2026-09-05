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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.R
import com.tripping.app.viewmodel.PingItem
import com.tripping.app.viewmodel.PingStatus
import com.tripping.app.ui.viewmodel.PingViewModel

// ===== 색상 설정 =====
private val BluePrimary = Color(0xFF4A72C4)
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)
private val CardBorder = Color(0xFFECECEC)
private val EmptyTextColor = Color(0xFFE6E6E6)

@Composable
internal fun PingRecordContent(
    modifier: Modifier = Modifier,
    viewModel: PingViewModel = viewModel(),
    onAddPingClick: () -> Unit,
    onPingLogClick: (Long) -> Unit,
    onRouteCardClick: (Long) -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadRecentTripWithPings()
    }

    val recentTrip = viewModel.recentTrip     // 여행 이름/날짜/장소수 (TripSummaryResponse)
    val pingDtos = viewModel.pings             // 핑 목록 (List<PingDto>) - 이제 바로 참조

    // 서버 응답 데이터를 UI 모델로 변환 (pingTime 안전하게 포맷팅)
    val pingsFromDb = pingDtos.map { dto ->
        val formattedTime = try {
            if (dto.pingTime.length >= 16) dto.pingTime.substring(11, 16) else dto.pingTime
        } catch (e: Exception) {
            "시간 미정"
        }

        PingItem(
            id = dto.pingId,
            placeName = dto.placeName,
            time = formattedTime,
            status = when (dto.isConfirmed) {
                true -> PingStatus.DONE
                else -> PingStatus.CURRENT
            }
        )
    }

    val hasTrip = recentTrip != null
    val canAddMorePing = pingsFromDb.size < 4

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Text(text = "여행을 기록해보아요!", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (!hasTrip) {
            item {
                EmptyTripView()
                Spacer(modifier = Modifier.height(16.dp))
            }
        } else {
            item {
                TimelineDots(pings = pingsFromDb.take(4))
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(pingsFromDb) { ping ->
                PingCard(ping = ping, onLinkClick = { onPingLogClick(ping.id) })
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (canAddMorePing) {
                item {
                    AddPingButton(onClick = onAddPingClick)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        item {
            HintBanner()
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "기록을 추가하고 싶은 핑로그가 있나요?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "바로가기", fontSize = 13.sp, color = GrayText, modifier = Modifier.clickable { })
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (recentTrip != null) {
                RouteCard(
                    title = recentTrip.representativeSpotName ?: "여행 기록",
                    dateText = recentTrip.travelDate,
                    pingCount = recentTrip.placeCount ?: 0,
                    onClick = { onRouteCardClick(recentTrip.tripId) }
                )
            }
        }
    }
}

@Composable
internal fun EmptyTripView() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.trip_empty_dino),
                contentDescription = "공룡",
                modifier = Modifier.width(120.dp).height(180.dp).alpha(0.20f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Image(
                painter = painterResource(id = R.drawable.trip_empty_window),
                contentDescription = "창문",
                modifier = Modifier.width(150.dp).height(210.dp).alpha(0.15f)
            )
        }
        Text(
            text = "아직 만들어진 여행이 없어요",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = EmptyTextColor
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
                    modifier = Modifier.weight(1f).height(2.dp).background(BluePrimary)
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
            text = "핑을 꾹 눌러 순서를 변경할 수 있어요 /  잊은 핑을 추가해보세요",
            fontSize = 11.sp,
            color = GrayText,
            modifier = Modifier.weight(1f)
        )
        Text(text = "×", fontSize = 13.sp, color = GrayText, modifier = Modifier.clickable { })
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
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(GrayBg)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = dateText, fontSize = 11.sp, color = GrayText)
        }
        Text(text = "핑 ${pingCount}개", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary)
    }
}

internal fun pingDrawable(status: PingStatus): Int = R.drawable.blueping