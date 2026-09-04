package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.ui.component.PlaceInfoCard
import com.tripping.app.viewmodel.PlaceCategory
import com.tripping.app.viewmodel.PlaceSearchViewModel
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.draw.shadow
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.PolylineOverlay
import com.naver.maps.map.compose.CircleOverlay
import com.naver.maps.map.overlay.OverlayImage
import com.tripping.app.R
private val regionOptions = listOf("전체", "서울", "강원", "부산", "제주")

@OptIn(com.naver.maps.map.compose.ExperimentalNaverMapApi::class)
@Composable
fun PlaceSearchScreen(
    viewModel: PlaceSearchViewModel = viewModel(),
    onPlaceClick: (Long) -> Unit = {}
) {
    val places by viewModel.places.collectAsState()
    val routes by viewModel.routes.collectAsState()
    var selectedPlace by remember { mutableStateOf<com.tripping.app.data.response.PlaceSearchResponse?>(null) }
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var regionMenuExpanded by remember { mutableStateOf(false) }
    var selectedRegion by remember { mutableStateOf("지역") }

    val pinIcon = remember { OverlayImage.fromResource(R.drawable.ic_map_pin) }

    LaunchedEffect(Unit) { viewModel.loadPlaces() }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ===== 상단 바: 지역 드롭다운 + 카테고리 탭 =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 20.dp, start = 15.dp, end = 15.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "동네핑거가 등록한",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF818181),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 지역 드롭다운
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { regionMenuExpanded = true }
                        ) {
                            Text(
                                text = selectedRegion,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.Black)
                        }
                        DropdownMenu(
                            expanded = regionMenuExpanded,
                            onDismissRequest = { regionMenuExpanded = false }
                        ) {
                            regionOptions.forEach { region ->
                                DropdownMenuItem(
                                    text = { Text(region) },
                                    onClick = {
                                        selectedRegion = region
                                        regionMenuExpanded = false
                                        // TODO: regionId 실제 코드로 변환해서 viewModel.onRegionSelected(...) 호출
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // 카테고리 탭 (관광지 / 맛집 / 카페)
                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PlaceCategory.entries.forEach { category ->
                            Text(
                                text = category.label,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedCategory == category) Color(0xFF22567E) else Color(0xFF818181),
                                modifier = Modifier.clickable { viewModel.onCategorySelected(category) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 검색바 + 시간대 + 혼잡도
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(58.dp, 38.dp)
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .background(Color.White, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "검색", tint = Color(0xFF818181))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterPill(text = "시간대", modifier = Modifier.weight(1f))

                    Spacer(modifier = Modifier.width(8.dp))

                    FilterPill(text = "Ping 개수", modifier = Modifier.weight(1f))

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(55.dp, 38.dp)
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .background(Color.White, RoundedCornerShape(24.dp))
                    )
                }
            }

            // ===== 지도 영역 (네이버맵 연결 예정) + 위에 뜨는 카드 =====
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition(LatLng(37.5665, 126.9780), 12.0) // 서울 시청 기준, 초기 줌 레벨 12
                }

                NaverMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { _, _ -> selectedPlace = null }
                ) {
                    // 루트별로 장소들을 방문 순서대로 이어서 선(경로) 그리기
                    routes.forEach { route ->
                        val sortedPlaces = route.places.sortedBy { it.visitOrder }
                        if (sortedPlaces.size >= 2) {
                            PolylineOverlay(
                                coords = sortedPlaces.map { LatLng(it.latitude, it.longitude) },
                                color = Color(0xFFD95A5A),
                                width = 8.dp
                            )
                        }
                    }

                    // 검색된 장소들을 커스텀 핀 아이콘으로 표시
                    places.forEach { place ->
                        Marker(
                            state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                            icon = pinIcon,
                            width = 34.dp,
                            height = 34.dp,
                            captionText = place.name,
                            onClick = {
                                selectedPlace = place
                                true
                            }
                        )
                    }
                }

                // 핑(마커)을 클릭했을 때만 카드 표시
                selectedPlace?.let { place ->
                    PlaceInfoCard(
                        place = place,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 100.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterPill(text: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .height(38.dp)
            .shadow(2.dp, RoundedCornerShape(24.dp))
            .background(Color.White, RoundedCornerShape(24.dp))
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF818181))
        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color(0xFF818181), modifier = Modifier.size(18.dp))
    }
}