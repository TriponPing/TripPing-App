package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.viewmodel.MyPageViewModel

// ===== 다녀온 여행 자세히보기 화면 (마이페이지 "다녀온 여행 > 자세히 보기") =====
// 실제 API(GET /users/me/trips) 연동됨.
// TODO: 지금은 1페이지(10건)만 불러옴 - 스크롤 페이징은 나중에 추가.

private val ColorBackground = Color(0xFFF8F8FC)
private val ColorLevelChipBg = Color(0xFFD9D9D9)

@Composable
fun TripHistoryScreen(
    onBackClick: () -> Unit = {},
    onOpenTripDetail: (Long, String) -> Unit = { _, _ -> },
    viewModel: MyPageViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        viewModel.loadAllTrips()
    }

    val profile by viewModel.profile.collectAsState()
    val allTrips by viewModel.allTrips.collectAsState()
    val trips = allTrips.map {
        MyPageTripCard(
            title = it.representativeSpotName ?: "여행 기록",
            dateRange = it.travelDate,
            routeId = it.tripId
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        // 상단: 마이페이지 라벨 + 프로필(간단 버전, 뱃지 없음)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(text = "마이페이지", fontSize = 12.sp, color = ColorTextSecondary)
            Spacer(modifier = Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 아바타 (TODO: 실제 프로필 이미지로 교체)
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8EEF5)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "👤", fontSize = 24.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = profile?.nickname ?: "불러오는 중...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(11.dp))
                            .background(ColorLevelChipBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = profile?.level?.let { "Lv.$it" } ?: "-",
                            fontSize = 12.sp,
                            color = ColorTextPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 뒤로가기 + 제목
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .clickable { onBackClick() }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "‹", fontSize = 20.sp, color = ColorTextPrimary)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "다녀온 여행", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
        }

        // 여행 목록 (TODO: 페이징 - GET /users/me/trips?page=&size=)
        if (trips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "다녀온 여행이 없어요", fontSize = 13.sp, color = ColorTextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trips) { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { trip.routeId?.let { onOpenTripDetail(it, trip.title) } }
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TripHistoryScreenPreview() {
    TripHistoryScreen()
}
