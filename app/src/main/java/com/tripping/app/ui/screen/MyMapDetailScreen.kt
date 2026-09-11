package com.tripping.app.ui.screen

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
//   GET /users/me/map/detail?type=  -> 탭별(drawn|saved|planned|places) 전체 경로
//   GET /users/me/map/search        -> 코스 이름 검색
// 지도는 네이버 지도 SDK(NCP Dynamic Map, Client ID는 AndroidManifest.xml에 등록) 사용.
// 상단 헤더 블록 없이 지도를 화면 꽉 채우고, 뒤로가기+검색창은 지도 위에 떠있게 배치함.
//
// 검색창 아래 탭 4개(스위치처럼 하나만 선택됨, 가로 스크롤):
//   "내 계획"     -> 아직 시작 안 한 계획(PLANNED) - 여행으로 전환하면 여기서 빠짐
//   "저장한 루트"  -> 남의 공개 루트를 북마크한 것(SAVED)
//   "여행보기"     -> 내가 실제로 다녀온 여행(DRAWN)
//   "저장한 장소"  -> 북마크(핀)로 저장한 단일 장소(PLACES) - 경로가 아니라 장소 하나하나라 선은 안 그려지고 핀만 찍힘

private val ColorAccentBlue = Color(0xFF0074CE)

private enum class MapTab(val apiType: String, val label: String, val lineColorHex: String) {
    PLANNED("planned", "내 계획", "#405AC8FA"), // 하늘색, 투명도 25% (CourseDetailScreen 등이랑 동일)
    SAVED("saved", "저장한 루트", "#FF7A3D"),
    VISITED("drawn", "여행보기", "#0074CE"),
    PLACES("places", "저장한 장소", "#00B894") // 스팟 1개짜리라 이 색은 실제로는 안 쓰임(선이 안 그려짐)
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
    // null = 세 탭 다 꺼진 상태(오버레이 없음). 탭을 눌러서 껐다 켰다 가능 - 같은 탭 다시 누르면 꺼짐.
    var selectedTab by remember { mutableStateOf<MapTab?>(MapTab.PLANNED) }
    var searchQuery by remember { mutableStateOf("") }
    var clickedName by remember { mutableStateOf<String?>(null) }
    // 카메라는 화면 진입 후 데이터가 처음 뜰 때 딱 한 번만 맞추고, 그 뒤로는 탭을 바꿔도
    // 지도를 움직이지 않음 (탭 전환할 때마다 지도가 튀는 게 불편하다는 피드백 반영)
    var hasFittedCamera by remember { mutableStateOf(false) }

    // 선택된 탭이 바뀔 때마다 해당 타입 데이터를 불러옴 (한 번 불러온 타입은 재요청 안 함 - ViewModel 캐시)
    LaunchedEffect(selectedTab) {
        selectedTab?.let { viewModel.loadMapDetail(it.apiType) }
    }

    // 선택된 탭의 루트 전체(경로선 + 마커)를 그림. 탭 바뀌면 이전 탭 오버레이는 지우고 새로 그림.
    // 탭이 null이면(다 꺼짐) 아무것도 안 그림.
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

        val tab = selectedTab
        if (map == null || tab == null) return@LaunchedEffect
        val list = detailByType[tab.apiType] ?: return@LaunchedEffect

        val allCoords = mutableListOf<LatLng>()

        list.forEach { route ->
            val sortedSpots = route.spots.sortedBy { it.visitOrder ?: 0 }
            val coords = sortedSpots.mapNotNull { s -> s.latitude?.let { la -> s.longitude?.let { lo -> LatLng(la, lo) } } }
            if (coords.isEmpty()) return@forEach
            allCoords.addAll(coords)

            if (coords.size >= 2) {
                val path = PathOverlay().apply {
                    this.coords = coords
                    color = AndroidColor.parseColor(tab.lineColorHex)
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

        if (!hasFittedCamera && allCoords.isNotEmpty()) {
            cameraUpdateToShowAll(allCoords)?.let { map.moveCamera(it) }
            hasFittedCamera = true
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
                        // 👈 수정: 이름이 없어서 날짜만 뜨던 것 -> 대표 스팟 이름(=이 앱에서 쓰는 "여행 이름")을 같이 표시
                        val label = result.spotName?.takeIf { it.isNotBlank() }
                            ?.let { "$it · ${result.travelDate}" }
                            ?: result.travelDate
                        Text(
                            text = "$icon $label",
                            fontSize = 13.sp,
                            color = ColorTextPrimary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // 👈 수정: 검색 응답이 이미 좌표를 갖고 있어서 pins 목록에서 다시 찾을 필요 없음
                                    clickedName = result.spotName
                                    val la = result.latitude
                                    val lo = result.longitude
                                    if (la != null && lo != null) {
                                        naverMap?.moveCamera(CameraUpdate.scrollAndZoomTo(LatLng(la, lo), 14.0))
                                    }
                                    searchQuery = ""
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 탭 4개 - 최대 하나만 선택되지만, 켜진 탭을 다시 누르면 꺼져서 넷 다 비활성화 가능.
            // 버튼이 커져서 한 화면에 다 안 들어갈 수 있어 가로 스크롤 가능하게 함.
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapTab.entries.forEach { tab ->
                    MapTabButton(
                        label = tab.label,
                        active = selectedTab == tab,
                        onClick = {
                            selectedTab = if (selectedTab == tab) null else tab
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
            .clip(RoundedCornerShape(24.dp))
            .background(if (active) ColorAccentBlue else Color.White)
            .clickable { onClick() }
            .padding(horizontal = 22.dp, vertical = 14.dp)
    ) {
        Text(
            text = label,
            fontSize = 15.sp,
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
