// [파일 설명] Ping 탭 > 특정 다녀온 여행(코스) 상세 화면 UI. 뒤로가기 + 코스 이름 + 그 여행의 핑 타임라인/목록을 보여주고, 핑 추가/수정으로 이어짐.
package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.viewmodel.PingItem
import com.tripping.app.viewmodel.PingStatus
import com.tripping.app.viewmodel.PingViewModel

@Composable
fun PingCourseDetailScreen(
    routeId: Long,
    courseName: String,
    viewModel: PingViewModel = viewModel(),
    onBackClick: () -> Unit,
    onAddPingClick: () -> Unit,
    onPingLogClick: (pingId: Long, placeName: String) -> Unit
) {
    // 이 코스(routeId)의 확정된 방문 스팟(ACTUAL_ROUTE_SPOT)을 불러옴
    LaunchedEffect(routeId) {
        viewModel.loadTripSpots(routeId)
    }

    val spots = viewModel.tripSpots

    val pingsFromDb = spots.mapIndexed { index, spot ->
        val formattedTime = try {
            spot.visitTime?.let { if (it.length >= 16) it.substring(11, 16) else it } ?: "시간 미정"
        } catch (e: Exception) {
            "시간 미정"
        }

        PingItem(
            id = spot.spotId ?: index.toLong(),
            placeName = spot.spotName ?: "이름 없음",
            time = formattedTime,
            status = PingStatus.DONE // 이미 확정된(다녀온) 방문 기록이라 전부 완료 상태
        )
    }

    val canAddMorePing = pingsFromDb.size < 4

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단바: 뒤로가기 + 코스 이름
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
            Text(text = courseName, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
        ) {
            // 핑이 있을 때만 타임라인 점 + 카드 목록 표시
            if (pingsFromDb.isNotEmpty()) {
                item {
                    TimelineDots(pings = pingsFromDb.take(4))
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(pingsFromDb) { ping ->
                    PingCard(ping = ping, onLinkClick = { onPingLogClick(ping.id, ping.placeName) })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // 👈 핑이 0개여도 + 버튼과 안내 배너는 항상 노출 (여기서 바로 추가할 수 있어야 하므로)
            if (canAddMorePing) {
                item {
                    AddPingButton(onClick = onAddPingClick)
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            item {
                HintBanner()
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}