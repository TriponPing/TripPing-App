package com.tripping.app.ui.screen

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.tripping.app.data.response.MapPinResponse
import com.tripping.app.ui.component.NaverMapContainer
import com.tripping.app.ui.component.cameraUpdateToShowAll
import com.tripping.app.viewmodel.MyPageViewModel
import kotlinx.coroutines.delay

// ===== 나의 여행 지도 자세히보기 화면 (마이페이지 "나의 여행 지도 > 자세히 보기") =====
// 실제 API 연동:
//   GET /users/me/map               -> 지도에 찍을 핀 전체 목록 (DRAWN=다녀온 여행 / SAVED=저장한 루트)
//   GET /users/me/map/detail?type=  -> 핀(또는 루트)을 클릭했을 때 그 타입의 전체 경로
//   GET /users/me/map/search        -> 코스 이름 검색
// 지도는 네이버 지도 SDK(NCP Dynamic Map, Client ID는 AndroidManifest.xml에 등록) 사용.

private val ColorAccentBlue = Color(0xFF0074CE)

@Composable
fun MyMapDetailScreen(
    onBackClick: () -> Unit = {},
    viewModel: MyPageViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadMyMap()
    }

    val pins by viewModel.mapPins.collectAsState()
    val detailByType by viewModel.mapDetailByType.collectAsState()
    val searchResults by viewModel.mapSearchResults.collectAsState()

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var selectedPin by remember { mutableStateOf<MapPinResponse?>(null) }
    var showTripOverlay by remember { mutableStateOf(false) }        // "여행 보기"/"여행 닫기" 토글
    var showSavedRoutesOverlay by remember { mutableStateOf(false) } // "내가 그린 루트 보기" 토글
    var searchQuery by remember { mutableStateOf("") }
    var clickedName by remember { mutableStateOf<String?>(null) }

    // ── 지도에 찍을 핀 전체 (pins가 바뀔 때마다 다시 그림) ──
    val pinMarkers = remember { mutableListOf<Marker>() }
    LaunchedEffect(naverMap, pins) {
        val map = naverMap ?: return@LaunchedEffect
        pinMarkers.forEach { it.map = null }
        pinMarkers.clear()

        pins.forEachIndexed { index, pin ->
            val lat = pin.latitude ?: return@forEachIndexed
            val lng = pin.longitude ?: return@forEachIndexed
            val marker = Marker().apply {
                position = LatLng(lat, lng)
                captionText = pin.representativeSpotName ?: "여행 ${index + 1}"
                iconTintColor = if (pin.type.equals("SAVED", ignoreCase = true))
                    AndroidColor.parseColor("#FF7A3D") else AndroidColor.parseColor("#0074CE")
                setOnClickListener {
                    selectedPin = pin
                    showTripOverlay = true
                    clickedName = pin.representativeSpotName
                    true
                }
                this.map = map
            }
            pinMarkers.add(marker)
        }

        val points = pins.mapNotNull { p -> p.latitude?.let { la -> p.longitude?.let { lo -> LatLng(la, lo) } } }
        cameraUpdateToShowAll(points)?.let { map.moveCamera(it) }
    }

    // ── "여행 보기" - 선택된 핀 하나의 전체 경로(방문 순서대로) ──
    val tripOverlayObjects = remember { mutableListOf<Any>() } // Marker | PathOverlay
    LaunchedEffect(naverMap, selectedPin, showTripOverlay, detailByType) {
        val map = naverMap
        tripOverlayObjects.forEach {
            when (it) {
                is Marker -> it.map = null
                is PathOverlay -> it.map = null
            }
        }
        tripOverlayObjects.clear()

        val pin = selectedPin
        if (map == null || pin == null || !showTripOverlay) return@LaunchedEffect

        val type = pin.type.lowercase()
        val list = detailByType[type] ?: run {
            viewModel.loadMapDetail(type)
            return@LaunchedEffect
        }
        val trip = list.find { it.tripId == pin.tripId } ?: return@LaunchedEffect
        val sortedSpots = trip.spots.sortedBy { it.visitOrder ?: 0 }
        val spotLatLngs = sortedSpots.mapNotNull { s -> s.latitude?.let { la -> s.longitude?.let { lo -> LatLng(la, lo) } } }
        if (spotLatLngs.isEmpty()) return@LaunchedEffect

        val lineColor = if (type == "saved") AndroidColor.parseColor("#FF7A3D") else AndroidColor.parseColor("#0074CE")

        if (spotLatLngs.size >= 2) {
            val path = PathOverlay().apply {
                coords = spotLatLngs
                color = lineColor
                width = 10
                setOnClickListener {
                    clickedName = trip.spots.firstOrNull()?.spotName
                    true
                }
                this.map = map
            }
            tripOverlayObjects.add(path)
        }

        sortedSpots.forEachIndexed { idx, spot ->
            val la = spot.latitude ?: return@forEachIndexed
            val lo = spot.longitude ?: return@forEachIndexed
            val marker = Marker().apply {
                position = LatLng(la, lo)
                captionText = "${spot.visitOrder ?: (idx + 1)}. ${spot.spotName ?: ""}"
                iconTintColor = lineColor
                setOnClickListener {
                    clickedName = spot.spotName
                    true
                }
                this.map = map
            }
            tripOverlayObjects.add(marker)
        }

        cameraUpdateToShowAll(spotLatLngs)?.let { map.moveCamera(it) }
    }

    // ── "내가 그린 루트 보기" - 저장한 루트(SAVED) 전체를 라인으로 표시, 각 루트 클릭 가능 ──
    val savedRouteOverlayObjects = remember { mutableListOf<PathOverlay>() }
    LaunchedEffect(naverMap, showSavedRoutesOverlay, detailByType) {
        val map = naverMap
        savedRouteOverlayObjects.forEach { it.map = null }
        savedRouteOverlayObjects.clear()

        if (map == null || !showSavedRoutesOverlay) return@LaunchedEffect

        val list = detailByType["saved"] ?: run {
            viewModel.loadMapDetail("saved")
            return@LaunchedEffect
        }
        list.forEach { trip ->
            val sortedSpots = trip.spots.sortedBy { it.visitOrder ?: 0 }
            val coords = sortedSpots.mapNotNull { s -> s.latitude?.let { la -> s.longitude?.let { lo -> LatLng(la, lo) } } }
            if (coords.size < 2) return@forEach
            val path = PathOverlay().apply {
                this.coords = coords
                color = AndroidColor.parseColor("#FF7A3D")
                width = 7
                // 루트 클릭 시 그 루트를 선택해서 "여행 보기" 상태로 자세히 표시 -> 루트 클릭 가능 요구사항
                setOnClickListener {
                    val pin = pins.find { p -> p.tripId == trip.tripId && p.type.equals("SAVED", true) }
                    if (pin != null) {
                        selectedPin = pin
                        showTripOverlay = true
                        clickedName = sortedSpots.firstOrNull()?.spotName
                    }
                    true
                }
                this.map = map
            }
            savedRouteOverlayObjects.add(path)
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
            Text(text = "나의 여행 지도 자세히보기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
        }

        Box(modifier = Modifier.weight(1f)) {
            NaverMapContainer(
                modifier = Modifier.fillMaxSize(),
                onMapReady = { naverMap = it }
            )

            // 검색창 + 검색 결과 드롭다운
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
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
                                            selectedPin = pin
                                            showTripOverlay = true
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

            // 우측 하단 토글 버튼 2개
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End
            ) {
                MapToggleButton(
                    label = if (showTripOverlay && selectedPin != null) "여행 닫기" else "여행 보기",
                    enabled = selectedPin != null,
                    onClick = { showTripOverlay = !showTripOverlay }
                )
                Spacer(modifier = Modifier.height(8.dp))
                MapToggleButton(
                    label = "내가 그린 루트 보기",
                    active = showSavedRoutesOverlay,
                    onClick = { showSavedRoutesOverlay = !showSavedRoutesOverlay }
                )
            }
        }
    }
}

@Composable
private fun MapToggleButton(
    label: String,
    enabled: Boolean = true,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (active) ColorAccentBlue else Color.White)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (active) Color.White else if (enabled) ColorAccentBlue else ColorTextSecondary
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MyMapDetailScreenPreview() {
    MyMapDetailScreen()
}
