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
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.tripping.app.ui.component.NaverMapContainer
import com.tripping.app.ui.component.applyPingIcon
import com.tripping.app.ui.component.cameraUpdateToShowAll
import com.tripping.app.viewmodel.MyPageViewModel


// ===== 코스(저장된거/그린거) 상세보기 화면 =====
// "다녀온 여행" 카드를 누르면 여기로 옴. GET /users/me/trips/{tripId} 데이터로 지도를 그려줌.
// "여행 바로 시작하기"는 전용 백엔드 API가 없어서, 같은 tripId로 다음 페이지(TripStartScreen)로
// 이동만 시켜줌 (거기서 같은 상세 데이터를 재사용해서 지도+미리보기를 보여줌).
// "코스 정보 바로가기"는 Ping 탭의 코스 상세(PingCourseDetailScreen)로 이동해서
// 이 여행(tripId=routeId)의 핑 기록이 바로 보이게 함.

private val ColorAccentPurple = Color(0xFF7B61FF)
private val ColorBackground = Color(0xFFF8F8FC)

@Composable
fun CourseDetailScreen(
    tripId: Long,
    fallbackTitle: String? = null,
    onBackClick: () -> Unit = {},
    onStartTrip: (Long, String) -> Unit = { _, _ -> },
    onGoToPing: (Long, String) -> Unit = { _, _ -> },
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
                color = android.graphics.Color.parseColor("#405AC8FA") // 하늘색, 투명도 25%
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
                applyPingIcon()
                this.map = map
            }
            overlayObjects.add(marker)
        }

        cameraUpdateToShowAll(spotLatLngs)?.let { map.moveCamera(it) }
    }

    val title = detail?.spots?.firstOrNull()?.spotName ?: fallbackTitle ?: "코스 상세"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        // 상단바 - 화살표(뒤로가기)만 단독으로, 코스명은 가운데 정렬
        // 배경 따로 안 줌 - 화면 전체(ColorBackground)랑 같은 톤으로 이어지게
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "‹",
                fontSize = 20.sp,
                color = ColorTextPrimary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .clickable { onBackClick() }
                    .padding(end = 8.dp, top = 2.dp, bottom = 2.dp)
            )
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.7f)
            )
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
                    .clickable { onStartTrip(tripId, title) }
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
                    .clickable { onGoToPing(tripId, title) }
                    .padding(vertical = 4.dp)
            )
        }
    }
}
