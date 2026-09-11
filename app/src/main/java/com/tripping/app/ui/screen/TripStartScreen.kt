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

// ===== "여행 바로 시작하기" 다음 페이지 =====
// CourseDetailScreen이랑 같은 tripId 데이터(GET /users/me/trips/{tripId})를 재사용해서
// 큰 지도 + 우측 하단 미니 지도 미리보기 카드 + "여행 계획 수정하기" 버튼을 보여줌.
// "여행 계획 수정하기"는 아직 전용 화면이 없어서, 일단 Ping 탭의 코스 상세(PingCourseDetailScreen)로
// 이동시켜줌 - 이때 이 여행(tripId=routeId)의 핑 기록이 바로 보이게 함.

private val ColorAccentBlue = Color(0xFF0074CE)
private val ColorBackground = Color(0xFFF8F8FC)

@Composable
fun TripStartScreen(
    tripId: Long,
    fallbackTitle: String? = null,
    onBackClick: () -> Unit = {},
    onGoToPing: (Long, String) -> Unit = { _, _ -> },
    viewModel: MyPageViewModel = viewModel()
) {
    LaunchedEffect(tripId) {
        viewModel.loadTripDetail(tripId)
    }

    val tripDetailById by viewModel.tripDetailById.collectAsState()
    val detail = tripDetailById[tripId]
    val title = detail?.spots?.firstOrNull()?.spotName ?: fallbackTitle ?: "코스"

    val sortedSpotLatLngs = remember(detail) {
        detail?.spots
            ?.sortedBy { it.visitOrder ?: 0 }
            ?.mapNotNull { s -> s.latitude?.let { la -> s.longitude?.let { lo -> LatLng(la, lo) } } }
            ?: emptyList()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        // 상단바 - 배경 따로 안 줌, 화면 전체(ColorBackground)랑 같은 톤으로 이어지게
        // 화살표는 단독으로, 코스명은 가운데 정렬 (CourseDetailScreen이랑 같은 스타일)
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
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth(0.7f)
            )
        }

        Box(modifier = Modifier.weight(1f)) {
            // 큰 지도 (경로 + 순서 마커)
            RouteMap(
                modifier = Modifier.fillMaxSize(),
                spotLatLngs = sortedSpotLatLngs,
                interactive = true,
                cameraPadding = 200
            )

            // 우측 하단: 미니 지도 미리보기 + 여행 계획 수정하기 버튼
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 180.dp, height = 140.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFE8EEF5))
                ) {
                    RouteMap(
                        modifier = Modifier.fillMaxSize(),
                        spotLatLngs = sortedSpotLatLngs,
                        interactive = false,
                        cameraPadding = 40
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ColorAccentBlue)
                        .clickable { onGoToPing(tripId, title) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(text = "여행 계획 수정하기", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

// 경로선 + 순서 마커를 그리는 지도. interactive=false면 미리보기용(제스처 비활성화)으로 그림.
@Composable
private fun RouteMap(
    modifier: Modifier = Modifier,
    spotLatLngs: List<LatLng>,
    interactive: Boolean,
    cameraPadding: Int
) {
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    val overlayObjects = remember { mutableListOf<Any>() } // Marker | PathOverlay

    LaunchedEffect(naverMap, spotLatLngs) {
        val map = naverMap
        overlayObjects.forEach {
            when (it) {
                is Marker -> it.map = null
                is PathOverlay -> it.map = null
            }
        }
        overlayObjects.clear()

        if (map == null || spotLatLngs.isEmpty()) return@LaunchedEffect

        if (spotLatLngs.size >= 2) {
            val path = PathOverlay().apply {
                coords = spotLatLngs
                color = android.graphics.Color.parseColor("#405AC8FA") // 하늘색, 투명도 25%
                width = if (interactive) 10 else 6
                this.map = map
            }
            overlayObjects.add(path)
        }

        spotLatLngs.forEachIndexed { idx, latLng ->
            val marker = Marker().apply {
                position = latLng
                if (interactive) {
                    captionText = "${idx + 1}"
                }
                applyPingIcon()
                this.map = map
            }
            overlayObjects.add(marker)
        }

        cameraUpdateToShowAll(spotLatLngs, paddingPx = cameraPadding)?.let { map.moveCamera(it) }
    }

    NaverMapContainer(
        modifier = modifier,
        onMapReady = { map ->
            if (!interactive) {
                map.uiSettings.apply {
                    isScrollGesturesEnabled = false
                    isZoomGesturesEnabled = false
                    isRotateGesturesEnabled = false
                    isTiltGesturesEnabled = false
                    isZoomControlEnabled = false
                    isCompassEnabled = false
                    isScaleBarEnabled = false
                    isLogoClickEnabled = false
                }
            }
            naverMap = map
        }
    )
}
