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
import com.tripping.app.viewmodel.PingViewModel

// ===== 기존 스타일에 맞춘 색상 설정 =====
private val BluePrimary = Color(0xFF4A72C4)
private val GrayText = Color(0xFF9A9A9A)
private val CardBorder = Color(0xFFECECEC)

/**
 * 💡 다녀온 여행 목록에 쓰일 데이터 모델 예시
 * (실제 프로젝트 구조에 맞게 데이터 클래스 위치를 조정하거나 대체하여 사용하세요)
 */
data class TripHistoryItem(
    val tripId: Long,
    val title: String,
    val dateText: String,
    val pingCount: Int,
    val durationText: String
)

@Composable
fun PingHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: PingViewModel = viewModel(),
    onBackClick: () -> Unit,
    onTripClick: (tripId: Long, title: String) -> Unit
) {
    // 💡 ViewModel로부터 다녀온 여행 목록 상태를 구독합니다.
    // (ViewModel에 tripHistoryList StateFlow 또는 LiveData가 선언되어 있다고 가정합니다)
    // val tripHistoryList by viewModel.tripHistoryList.collectAsState()

    // 💡 ViewModel 연동 전 테스트를 원하신다면 아래 임시 리스트를 ViewModel 상태값으로 대체하세요.
    val sampleTripList = remember {
        listOf(
            TripHistoryItem(1L, "성수 나들이 코스2", "2026.08.13 ~ 08.14", 3, "1일 1시간 6분"),
            TripHistoryItem(2L, "성수 나들이 코스2", "2026.08.13 ~ 08.14", 2, "1일 1시간 6분"),
            TripHistoryItem(3L, "성수 나들이 코스2", "2026.08.13 ~ 08.14", 1, "1일 1시간 6분")
        )
    }

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

        // 다녀온 여행 카드 리스트 (하드코딩 제거 및 items 반복문 적용)
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(sampleTripList) { trip ->
                FinishedTripCard(
                    title = trip.title,
                    dateText = trip.dateText,
                    pingCount = trip.pingCount,
                    durationText = trip.durationText,
                    statusDotsCount = trip.pingCount, // 핑 개수에 맞춰 타임라인 닷 개수 연동
                    onClick = { onTripClick(trip.tripId, trip.title) }
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
            // 상단: 제목 (저장 아이콘 제거 완료)
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

        // 우측 하단 정보 (핑 개수 및 소요 시간)
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