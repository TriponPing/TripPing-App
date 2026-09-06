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
//   GET /users/me/map               -> 검색 결과 클릭 시 좌표 찾는 용도로만 사용
//   GET /users/me/map/detail?type=  -> 탭별(drawn|saved|planned) 전체 경로
//   GET /users/me/map/search        -> 코스 이름 검색
// 지도는 네이버 지도 SDK(NCP Dynamic Map, Client ID는 AndroidManifest.xml에 등록) 사용.
// 상단 헤더 블록 없이 지도를 화면 꽉 채우고, 뒤로가기+검색창은 지도 위에 떠있게 배치함.
//
// 검색창 아래 탭 3개(스위치처럼 하나만 선택됨):
//   "내 계획"    -> 아직 시작 안 한 계획(PLANNED) - 여행으로 전환하면 여기서 빠짐
//   "저장한 루트" -> 남의 공개 루트를 북마크한 것(SAVED)
//   "여행보기"    -> 내가 실제로 다녀온 여행(DRAWN)

private val ColorAccentBlue = Color(0xFF0074CE)

private enum class MapTab(val apiType: String, val label: String, val lineColorHex: String) {
    PLANNED("planned", "내 계획", "#34C759"),
    SAVED("saved", "저장한 루트", "#FF7A3D"),
    VISITED("drawn", "여행보기", "#0074CE")
}

@Composable
fun MyMapDetailScreen(
    onBackClick: () -> Unit = {},
    viewModel: MyPageViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadMyMap() // 검색 결과 클릭 시 좌표 찾는 용도
    }

    val pins by viewModel.mapPins.collectAsState()
    val detailByType by viewModel.mapDetailByType.collectAsState()
    val searchResults by viewModel.mapSearchResults.collectAsState()

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var selectedTab by remember { mutableStateOf(MapTab.PLANNED) }
    var searchQuery by remember { mutableStateOf("") }
    var clickedName by remember { mutableStateOf<String?>(null) }

    // 선택된 탭이 바뀔 때마다 해당 타입 데이터를 불러옴 (한 번 불러온 타입은 재요청 안 함 - ViewModel 캐시)
    LaunchedEffect(selectedTab) {
        viewModel.loadMapDetail(selectedTab.apiType)
    }

    // 선택된 탭의 루트 전체(경로선 + 마커)를 그림. 탭 바뀌면 이전 탭 오버레이는 지우고 새로 그림.
    val overlayObjects = remember { mutableListOf<Any>() } // Marker | PathOverlay
    LaunchedEffect(naverMap, selectedTab, detailByType) {
        val map = naverMap
        overlayObjects.forEach {
            when (it) {
                is Marker -> it.map = null
                is PathOverlay -> it.map = null
            }
        }
        overlayObjects.clear()

        if (map == null) return@LaunchedEffect
        val list = detailByType[selectedTab.apiType] ?: return@LaunchedEffect

        val allCoords = mutableListOf<LatLng>()

        list.forEach { route ->
            val sortedSpots = route.spots.sortedBy { it.visitOrder ?: 0 }
            val coords = sortedSpots.mapNotNull { s -> s.latitude?.let { la -> s.longitude?.let { lo -> LatLng(la, lo) } } }
            if (coords.isEmpty()) return@forEach
            allCoords.addAll(coords)

            if (coords.size >= 2) {
                val path = PathOverlay().apply {
                    this.coords = coords
                    color = AndroidColor.parseColor(selectedTab.lineColorHex)
                    width = 7
                    setOnClickListener {
                        clickedName = sortedSpots.firstOrNull()?.spotName
                        true
                    }
                    this.map = map
                }
                overlayObjects.add(path)
            }

            sortedSpots.forEach { spot ->
                val la = spot.latitude ?: return@forEach
                val lo = spot.longitude ?: return@forEach
                val marker = Marker().apply {
                    position = LatLng(la, lo)
                    captionText = spot.spotName ?: ""
                    applyPingIcon()
                    setOnClickListener {
                        clickedName = spot.spotName
                        true
                    }
                    this.map = map
                }
                overlayObjects.add(marker)
            }
        }

        cameraUpdateToShowAll(allCoords)?.let { map.moveCamera(it) }
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

        // 뒤로가기 + 검색창 + 탭 3개 - 헤더 블록 없이 지도 위에 공중에 떠있게 배치
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

            Spacer(modifier = Modifier.height(12.dp))

            // 탭 3개 - 스위치처럼 하나만 선택됨
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MapTab.entries.forEach { tab ->
                    MapTabButton(
                        label = tab.label,
                        active = selectedTab == tab,
                        onClick = {
                            selectedTab = tab
                            clickedName = null
                        }
                    )
                }
            }
        }

        // 지도 위 핀/루트를 클릭했을 때 이름 안내
        clickedName?.let { name ->
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 150.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xCC000000))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(text = name, fontSize = 12.sp, color = Color.White)
            }
        }
    }
}

@Composable
private fun MapTabButton(
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
