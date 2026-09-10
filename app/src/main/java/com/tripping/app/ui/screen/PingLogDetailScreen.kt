// [파일 설명] Ping 탭 > 로그 커뮤니티 > "로그 상세보기" 화면 UI. 그 루트의 경유지 미리보기 + 후기(리뷰) 목록을 보여줌.
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.data.response.RouteReviewResponse
import com.tripping.app.viewmodel.CommunityViewModel
import com.tripping.app.viewmodel.PingItem
import com.tripping.app.viewmodel.PingStatus
import com.tripping.app.viewmodel.PingViewModel

private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)

@Composable
fun PingLogDetailScreen(
    routeId: Long,
    onBackClick: () -> Unit,
    pingViewModel: PingViewModel = viewModel(),
    communityViewModel: CommunityViewModel = viewModel()
) {
    LaunchedEffect(routeId) {
        pingViewModel.loadTripSpots(routeId)
        communityViewModel.loadReviews(routeId)
    }

    val spots = pingViewModel.tripSpots
    val reviews = communityViewModel.reviews

    val pingsFromDb = spots.mapIndexed { index, spot ->
        PingItem(
            id = spot.actualRouteSpotId ?: (spot.spotId ?: index.toLong()),
            placeName = spot.spotName ?: "이름 없음",
            time = "",
            status = PingStatus.DONE
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // 상단바: 뒤로가기 + 타이틀
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹",
                fontSize = 22.sp,
                color = Color.Black,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(end = 12.dp)
            )
            Text(text = "로그 상세보기", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            if (pingsFromDb.isNotEmpty()) {
                item {
                    TimelineDots(pings = pingsFromDb.take(4))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = pingsFromDb.joinToString(" → ") { it.placeName },
                        fontSize = 13.sp,
                        color = Color(0xFF333333)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            item {
                Text(text = "Ping 로그 후기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (reviews.isEmpty()) {
                item {
                    Text(
                        text = if (communityViewModel.isLoadingReviews) "불러오는 중..." else "아직 등록된 후기가 없어요",
                        fontSize = 13.sp,
                        color = GrayText,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(reviews) { review ->
                    ReviewCard(review = review)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: RouteReviewResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GrayBg)
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8EEF5))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = review.writerNickname, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = review.content, fontSize = 13.sp, color = Color(0xFF333333))
        }
    }
}
