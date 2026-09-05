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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.R
import com.tripping.app.viewmodel.MyPageViewModel // 👈 MyPageViewModel 임포트

// ===== 기존 스타일에 맞춘 색상 설정 =====
private val BluePrimary = Color(0xFF4A72C4)
private val GrayText = Color(0xFF9A9A9A)
private val CardBorder = Color(0xFFECECEC)

@Composable
fun PingHistoryScreen(
    modifier: Modifier = Modifier,
    myPageViewModel: MyPageViewModel = viewModel(), // 👈 MyPageViewModel을 사용합니다
    onBackClick: () -> Unit,
    onTripClick: (tripId: Long, title: String) -> Unit
) {
    // 💡 화면 진입 시 MyPageViewModel을 통해 다녀온 여행 전체 목록 데이터를 불러옵니다.
    LaunchedEffect(Unit) {
        myPageViewModel.loadAllTrips()
    }

    // 💡 MyPageViewModel에 정의된 allTrips StateFlow를 구독합니다.
    val allTrips by myPageViewModel.allTrips.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 상단 뒤로가기 및 타이틀 영역
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.chevron_left),
                contentDescription = "뒤로가기",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { onBackClick() }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "다녀온 여행",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 서버에서 받아온 실제 여행 목록(allTrips)을 반복문으로 출력
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allTrips) { trip ->
                FinishedTripCard(
                    title = trip.representativeSpotName ?: "여행 기록",
                    dateText = trip.travelDate,
                    pingCount = trip.placeCount ?: 0,
                    durationText = trip.travelDate, // 👈 날짜 혹은 필요하신 텍스트 대체
                    statusDotsCount = trip.placeCount ?: 0, // 핑 개수에 맞춰 타임라인 닷 개수 연동
                    onClick = { onTripClick(trip.tripId, trip.representativeSpotName ?: "여행 기록") }
                )
            }
        }
    }
}

@Composable
internal fun FinishedTripCard(
    title: String,
    dateText: String,
    pingCount: Int,
    durationText: String,
    statusDotsCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            // 상단: 제목
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(2.dp))
            Text(text = dateText, fontSize = 11.sp, color = GrayText)

            Spacer(modifier = Modifier.height(14.dp))

            // 미니 타임라인 닷 시각화 영역
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(statusDotsCount) { index ->
                    Image(
                        painter = painterResource(id = R.drawable.blueping),
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    if (index != statusDotsCount - 1) {
                        Box(
                            modifier = Modifier
                                .width(24.dp)
                                .height(2.dp)
                                .background(BluePrimary)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 우측 하단 정보 (핑 개수 및 날짜/소요 시간)
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "핑 ${pingCount}개",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BluePrimary
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = durationText,
                fontSize = 11.sp,
                color = GrayText
            )
        }
    }
}

// ==========================================
// 💡 프리뷰 함수
// ==========================================
@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PingHistoryScreenPreview() {
    PingHistoryScreen(
        onBackClick = {},
        onTripClick = { _, _ -> }
    )
}