package com.tripping.app.ui.screen

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.tripping.app.ui.component.NaverMapContainer
import com.tripping.app.ui.component.applyPingIcon
import com.tripping.app.ui.component.cameraUpdateToShowAll
import com.tripping.app.viewmodel.MyPageViewModel
import kotlinx.coroutines.delay

// ===== 나의 여행 지도 자세히보기 화면 (마이페이지 "나의 여행 지도 > 자세히 보기") =====
// 실제 API 연동:
//   GET /users/me/map               -> 지도에 찍을 핀 전체 목록 (DRAWN=다녀온 여행 / SAVED=저장한 루트)
//   GET /users/me/map/detail?type=  -> 타입별(drawn|saved) 전체 경로 - "내가 그린 루트 보기" 토글용
//   GET /users/me/map/search        -> 코스 이름 검색
// 지도는 네이버 지도 SDK(NCP Dynamic Map, Client ID는 AndroidManifest.xml에 등록) 사용.
// Figma대로 상단 헤더 블록 없이 지도를 화면 꽉 채우고, 뒤로가기+검색창은 지도 위에 떠있게 배치함.
//
// 버튼 2개:
//   "여행 보기"          -> 지도 위 핀(다녀온 여행 + 저장한 루트) 전체를 껐다 켰다
//   "내가 그린 루트 보기" -> 내가 다녀온 여행(DRAWN)의 경로 연결선을 껐다 켰다
//                          ("내가 그린" = 남한테 저장(북마크)한 루트가 아니라 내가 직접 다닌 내 루트)

private val ColorAccentBlue = Color(0xFF0074CE)
private const val TYPE_DRAWN = "drawn"
private const val TYPE_SAVED = "saved"

@Composable
fun MyMapDetailScreen(
    onBackClick: () -> Unit = {},
    viewModel: MyPageViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadMyMap()
        // "여행 보기"가 여행 하나당 대표 장소 1개짜리 요약 핀이 아니라 실제 방문 장소를
        // 전부 보여주도록, 다녀온 여행/저장한 루트 둘 다 상세(전체 스팟)를 미리 불러옴
        viewModel.loadMapDetail(TYPE_DRAWN)
        viewModel.loadMapDetail(TYPE_SAVED)
    }

    val pins by viewModel.mapPins.collectAsState()
    val detailByType by viewModel.mapDetailByType.collectAsState()
    val searchResults by viewModel.mapSearchResults.collectAsState()

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var showPins by remember { mutableStateOf(true) } // "여행 보기" - 핀 전체 표시 여부
    var showDrawnRouteLines by remember { mutableStateOf(false) } // "내가 그린 루트 보기" - 내 다녀온 여행 경로선 표시 여부
    var searchQuery by remember { mutableStateOf("") }
    var clickedName by remember { mutableStateOf<String?>(null) }

    // "여행 보기"에 실제로 찍을 방문 장소 전체 - 다녀온 여행 + 저장한 루트 안의 모든 스팟을 펼침
    // (GET /users/me/map은 여행 하나당 대표 장소 1개짜리 요약 핀이라, 여러 장소를 들른 여행은
    //  대표 장소 하나만 보이는 문제가 있었음 -> 상세(map/detail) 데이터를 스팟 단위로 다 풀어서 사용)
    val allVisitedSpots = remember(detailByType) {
        val drawn = detailByType[TYPE_DRAWN].orEmpty()
        val saved = detailByType[TYPE_SAVED].orEmpty()
        (drawn + saved).flatMap { it.spots }
    }

    // 핀이 새로 로드되면 전부 보이도록 카메라 위치 맞춤 (핀을 껐다 켜는 것과 무관하게 최초 1회성 성격)
    LaunchedEffect(naverMap, allVisitedSpots, pins) {
        val map = naverMap ?: return@LaunchedEffect
        val detailPoints = allVisitedSpots.mapNotNull { s -> s.latitude?.let { la -> s.longitude?.let { lo -> LatLng(la, lo) } } }
        val points = detailPoints.ifEmpty {
            pins.mapNotNull { p -> p.latitude?.let { la -> p.longitude?.let { lo -> LatLng(la, lo) } } }
        }
        cameraUpdateToShowAll(points)?.let { map.moveCamera(it) }
    }

    // ── 지도에 찍을 핀 전체(방문 장소 하나하나) - "여행 보기" 토글로 껐다 켰다. 탭하면 이름만 안내 ──
    val pinMarkers = remember { mutableListOf<Marker>() }
    LaunchedEffect(naverMap, allVisitedSpots, showPins) {
        val map = naverMap
        pinMarkers.forEach { it.map = null }
        pinMarkers.clear()

        if (map == null || !showPins) return@LaunchedEffect

        allVisitedSpots.forEach { spot ->
            val lat = spot.latitude ?: return@forEach
            val lng = spot.longitude ?: return@forEach
            val marker = Marker().apply {
                position = LatLng(lat, lng)
                captionText = spot.spotName ?: ""
                applyPingIcon()
                setOnClickListener {
                    clickedName = spot.spotName
                    true
                }
                this.map = map
            }
            pinMarkers.add(marker)
        }
    }

    // ── "내가 그린 루트 보기" - 내가 다녀온 여행(DRAWN) 전체를 라인으로 표시, 각 루트 클릭 가능 ──
    val drawnRouteOverlayObjects = remember { mutableListOf<Any>() } // Marker | PathOverlay
    LaunchedEffect(naverMap, showDrawnRouteLines, detailByType) {
        val map = naverMap
        drawnRouteOverlayObjects.forEach {
            when (it) {
                is Marker -> it.map = null
                is PathOverlay -> it.map = null
            }
        }
        drawnRouteOverlayObjects.clear()

        if (map == null || !showDrawnRouteLines) return@LaunchedEffect

        val list = detailByType[TYPE_DRAWN] ?: run {
            viewModel.loadMapDetail(TYPE_DRAWN)
            return@LaunchedEffect
        }

        list.forEach { trip ->
            val sortedSpots = trip.spots.sortedBy { it.visitOrder ?: 0 }
            val coords = sortedSpots.mapNotNull { s -> s.latitude?.let { la -> s.longitude?.let { lo -> LatLng(la, lo) } } }
            if (coords.isEmpty()) return@forEach

            if (coords.size >= 2) {
                val path = PathOverlay().apply {
                    this.coords = coords
                    color = AndroidColor.parseColor("#0074CE")
                    width = 7
                    setOnClickListener {
                        clickedName = sortedSpots.firstOrNull()?.spotName
                        true
                    }
                    this.map = map
                }
                drawnRouteOverlayObjects.add(path)
            }

            sortedSpots.forEach { spot ->
                val la = spot.latitude ?: return@forEach
                val lo = spot.longitude ?: return@forEach
                val marker = Marker().apply {
                    position = LatLng(la, lo)
                    applyPingIcon()
                    setOnClickListener {
                        clickedName = spot.spotName
                        true
                    }
                    this.map = map
                }
                drawnRouteOverlayObjects.add(marker)
            }
        }
    }

    // 검색 디바운스 (300ms)
    LaunchedEffect(searchQuery) {
        if (searchQuery.isBlank()) {
            viewModel.clearMapSearch()
        } else {
            delay(300)
            viewModel.searchMyMap(searchQuery)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NaverMapContainer(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { naverMap = it }
        )

        // 뒤로가기 + 검색창 - 헤더 블록 없이 지도 위에 공중에 떠있게 배치
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onBackClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "‹", fontSize = 20.sp, color = ColorTextPrimary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp)),
                    placeholder = { Text("코스 이름으로 찾아보세요!", fontSize = 13.sp, color = ColorTextSecondary) },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
            }

            if (searchQuery.isNotBlank() && searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                ) {
                    searchResults.forEach { result ->
                        val icon = if (result.type.equals("SAVED", true)) "🔖" else "📍"
                        Text(
                            text = "$icon ${result.travelDate}",
                            fontSize = 13.sp,
                            color = ColorTextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val pin = pins.find { it.tripId == result.tripId && it.type.equals(result.type, true) }
                                    if (pin != null) {
                                        clickedName = pin.representativeSpotName
                                        val la = pin.latitude
                                        val lo = pin.longitude
                                        if (la != null && lo != null) {
                                            naverMap?.moveCamera(CameraUpdate.scrollAndZoomTo(LatLng(la, lo), 14.0))
                                        }
                                    }
                                    searchQuery = ""
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        // 지도 위 핀/루트를 클릭했을 때 이름 안내
        clickedName?.let { name ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 92.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = name, fontSize = 12.sp, color = Color.White)
            }
        }

        // 우측 하단 토글 버튼 2개 - 둘 다 그냥 온/오프 토글(문구는 안 바뀌고 색깔만 바뀜)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            MapToggleButton(
                label = "여행 보기",
                active = showPins,
                onClick = { showPins = !showPins }
            )
            Spacer(modifier = Modifier.height(8.dp))
            MapToggleButton(
                label = "내가 그린 루트 보기",
                active = showDrawnRouteLines,
                onClick = { showDrawnRouteLines = !showDrawnRouteLines }
            )
        }
    }
}

@Composable
private fun MapToggleButton(
    label: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) ColorAccentBlue else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else ColorAccentBlue
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyMapDetailScreenPreview() {
    MyMapDetailScreen()
}
