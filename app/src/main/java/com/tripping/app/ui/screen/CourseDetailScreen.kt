package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.tripping.app.ui.component.NaverMapContainer
import com.tripping.app.viewmodel.MyPageViewModel

// ===== 코스(저장된거/그린거) 상세보기 화면 =====
// "다녀온 여행" 카드를 누르면 여기로 옴. GET /users/me/trips/{tripId} 데이터로 지도를 그려줌.
// "여행 바로 시작하기" / "코스 정보 바로가기"는 아직 백엔드 API 확정 전이라 TODO로 비워둠
// (김동혜 담당 "여행 바로 시작하기" API - 팀장님 확인 중, 확정되면 여기 onStartTripClick에 연결).

private val ColorAccentPurple = Color(0xFF7B61FF)

@Composable
fun CourseDetailScreen(
    tripId: Long,
    fallbackTitle: String? = null,
    onBackClick: () -> Unit = {},
    viewModel: MyPageViewModel = viewModel()
) {
    LaunchedEffect(tripId) {
        viewModel.loadTripDetail(tripId)
    }

    val tripDetailById by viewModel.tripDetailById.collectAsState()
    val detail = tripDetailById[tripId]

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    // 지도에 경로선 + 순서 마커 그리기
    val overlayObjects = remember { mutableListOf<Any>() } // Marker | PathOverlay
    LaunchedEffect(naverMap, detail) {
        val map = naverMap
        overlayObjects.forEach {
            when (it) {
                is Marker -> it.map = null
                is PathOverlay -> it.map = null
            }
        }
        overlayObjects.clear()

        if (map == null || detail == null) return@LaunchedEffect

        val sortedSpots = detail.spots.sortedBy { it.visitOrder ?: 0 }
        val spotLatLngs = sortedSpots.mapNotNull { s -> s.latitude?.let { la -> s.longitude?.let { lo -> LatLng(la, lo) } } }
        if (spotLatLngs.isEmpty()) return@LaunchedEffect

        if (spotLatLngs.size >= 2) {
            val path = PathOverlay().apply {
                coords = spotLatLngs
                color = android.graphics.Color.parseColor("#0074CE")
                width = 10
                this.map = map
            }
            overlayObjects.add(path)
        }

        sortedSpots.forEachIndexed { idx, spot ->
            val la = spot.latitude ?: return@forEachIndexed
            val lo = spot.longitude ?: return@forEachIndexed
            val marker = Marker().apply {
                position = LatLng(la, lo)
                captionText = "${spot.visitOrder ?: (idx + 1)}. ${spot.spotName ?: ""}"
                iconTintColor = android.graphics.Color.parseColor("#0074CE")
                this.map = map
            }
            overlayObjects.add(marker)
        }

        LatLngBounds.fromOrNull(spotLatLngs)?.let { bounds ->
            map.moveCamera(CameraUpdate.fitBounds(bounds, 150))
        }
    }

    val title = detail?.spots?.firstOrNull()?.spotName ?: fallbackTitle ?: "코스 상세"

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단바
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .clickable { onBackClick() }
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "‹", fontSize = 20.sp, color = ColorTextPrimary)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "코스 ( 저장된거 , 그린거 ) 보기", fontSize = 12.sp, color = ColorTextSecondary)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
        }

        // 지도
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(20.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFE8EEF5))
        ) {
            NaverMapContainer(
                modifier = Modifier.fillMaxSize(),
                onMapReady = { naverMap = it }
            )
        }

        // 액션 버튼 2개
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(ColorAccentPurple)
                    .clickable {
                        // TODO: 김동혜 팀원이 만든 "여행 바로 시작하기" API 확정되면 여기 연결
                        // (백엔드 POST /routes/{routeId}/trips는 routeId가 PlannedRoute 기준이라
                        //  다녀온 여행(ActualRoute) tripId를 그대로 못 씀 - 팀장님/김동혜님 확인 필요)
                    }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "여행 바로 시작하기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "코스 정보 바로가기",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // TODO: 코스 상세 정보 화면 나오면 연결
                    }
                    .padding(vertical = 4.dp)
            )
        }
    }
}
