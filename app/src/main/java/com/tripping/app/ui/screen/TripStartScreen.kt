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

// ===== "여행 바로 시작하기" 다음 페이지 =====
// CourseDetailScreen이랑 같은 tripId 데이터(GET /users/me/trips/{tripId})를 재사용해서
// 큰 지도 + 우측 하단 미니 지도 미리보기 카드 + "여행 계획 수정하기" 버튼을 보여줌.
// "여행 계획 수정하기"는 루트(장소 추가/순서변경) 도메인이라 아직 TODO로 비워둠.

private val ColorAccentBlue = Color(0xFF0074CE)

@Composable
fun TripStartScreen(
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
    val title = detail?.spots?.firstOrNull()?.spotName ?: fallbackTitle ?: "코스"

    val sortedSpotLatLngs = remember(detail) {
        detail?.spots
            ?.sortedBy { it.visitOrder ?: 0 }
            ?.mapNotNull { s -> s.latitude?.let { la -> s.longitude?.let { lo -> LatLng(la, lo) } } }
            ?: emptyList()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 상단바
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
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
        }

        Box(modifier = Modifier.weight(1f)) {
            // 큰 지도 (경로 + 순서 마커)
            RouteMap(
                modifier = Modifier.fillMaxSize(),
                spotLatLngs = sortedSpotLatLngs,
                interactive = true,
                cameraPadding = 150
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
                        .clickable {
                            // TODO: 루트 장소 수정 화면(루트 도메인) 나오면 연결
                        }
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
                color = android.graphics.Color.parseColor("#0074CE")
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
                iconTintColor = android.graphics.Color.parseColor("#0074CE")
                this.map = map
            }
            overlayObjects.add(marker)
        }

        LatLngBounds.fromOrNull(spotLatLngs)?.let { bounds ->
            map.moveCamera(CameraUpdate.fitBounds(bounds, cameraPadding))
        }
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
