package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.PolylineOverlay
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.overlay.OverlayImage
import com.tripping.app.R
import com.tripping.app.data.response.PlaceSearchResponse
import com.tripping.app.ui.component.PlaceInfoCard
import com.tripping.app.viewmodel.PlaceCategory
import com.tripping.app.viewmodel.PlaceSearchViewModel
import androidx.compose.foundation.layout.offset
import androidx.compose.material.icons.filled.KeyboardArrowUp

private val regionOptions = listOf("전체", "서울", "강원", "부산", "제주")
private val timeSlotOptions = listOf("전체", "아침", "오전", "오후", "저녁", "밤/새벽")
private val pingCountOptions = listOf("전체" to null, "3개" to 3, "4개 이상" to 4, "5개 이상" to 5, "6개 이상" to 6)

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun PlaceSearchScreen(
    viewModel: PlaceSearchViewModel = viewModel(),
    onPlaceClick: (Long) -> Unit = {}
) {
    val places by viewModel.places.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTimeSlot by viewModel.selectedTimeSlot.collectAsState()
    val selectedMinPingCount by viewModel.selectedMinPingCount.collectAsState()

    var regionMenuExpanded by remember { mutableStateOf(false) }
    var selectedRegion by remember { mutableStateOf("지역") }
    var timeSlotMenuExpanded by remember { mutableStateOf(false) }
    var pingCountMenuExpanded by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<PlaceSearchResponse?>(null) }

    val pinIcon = remember { OverlayImage.fromResource(R.drawable.ic_map_pin) }

    // 화면 처음 진입 시: 카테고리 미선택 상태이므로 전체 루트/핑 로드
    LaunchedEffect(Unit) { viewModel.loadAllRoutes() }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // ===== 상단 헤더 =====
        // ===== 상단 헤더: 피그마 좌표 그대로 절대 배치 (동네핑거 문구 / 지역 / 카테고리 탭) =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Color.White)
        ) {
            // "동네핑거가 등록한" — left:90 top:16 width:208 height:25
            Text(
                text = "동네핑거가 등록한",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF818181),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .offset(x = 90.dp, y = 16.dp)
                    .width(208.dp)
                    .height(25.dp)
            )

            // "지역" 드롭다운 — left:14 top:32
            Box(
                modifier = Modifier
                    .offset(x = 14.dp, y = 32.dp)
            ) {
                RegionDropdownField(
                    selectedRegion = selectedRegion,
                    expanded = regionMenuExpanded,
                    onExpandedChange = { regionMenuExpanded = it },
                    onRegionSelected = { selectedRegion = it }
                )
            }

            // 카테고리 탭 박스 — left:156 top:44 width:242 height:33 (오른쪽 여백을 지역 왼쪽 여백과 동일하게 14dp로 맞춤)
            Row(
                modifier = Modifier
                    .offset(x = 156.dp, y = 44.dp)
                    .width(242.dp)
                    .height(33.dp)
                    .background(Color.White, RoundedCornerShape(8.dp))
                    .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(8.dp))
                    .padding(horizontal = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                PlaceCategory.entries.forEach { category ->
                    val isSelected = selectedCategory == category
                    Text(
                        text = category.label,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) Color(0xFF0074CE) else Color(0xFF818181),
                        modifier = Modifier.clickable { viewModel.onCategorySelected(category) }
                    )
                }
            }
        }

        // ===== 지도 영역: 지도 + (카테고리 미선택 시만) 검색/필터바 + 장소 카드 =====
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition(LatLng(37.5665, 126.9780), 12.0)
            }

            NaverMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { _, _ -> selectedPlace = null }
            ) {
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

                if (selectedCategory == null) {
                    // 카테고리 미선택: 루트에 등록된 장소들을 전부 핑으로 표시 (중복 제거)
                    val routePlaces = routes.flatMap { it.places }.distinctBy { it.spotId }
                    routePlaces.forEach { routePlace ->
                        Marker(
                            state = MarkerState(position = LatLng(routePlace.latitude, routePlace.longitude)),
                            icon = pinIcon,
                            width = 34.dp,
                            height = 34.dp,
                            captionText = routePlace.spotName,
                            onClick = {
                                selectedPlace = PlaceSearchResponse(
                                    spotId = routePlace.spotId,
                                    name = routePlace.spotName,
                                    category = "",
                                    address = "",
                                    latitude = routePlace.latitude,
                                    longitude = routePlace.longitude,
                                    imageUrl = null,
                                    description = null
                                )
                                true
                            }
                        )
                    }
                } else {
                    // 카테고리 선택됨: 해당 카테고리 검색 결과만 핑으로 표시
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
            }

            // 카테고리가 선택 안 된 상태에서만 검색/필터바 표시
            if (selectedCategory == null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 14.dp, start = 16.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
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

                    Box(modifier = Modifier.width(131.dp)) {
                        FilterPill(
                            text = selectedTimeSlot ?: "시간대",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { timeSlotMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = timeSlotMenuExpanded,
                            onDismissRequest = { timeSlotMenuExpanded = false }
                        ) {
                            timeSlotOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        viewModel.onTimeSlotSelected(if (option == "전체") null else option)
                                        timeSlotMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(modifier = Modifier.width(131.dp)) {
                        FilterPill(
                            text = pingCountOptions.find { it.second == selectedMinPingCount }?.first
                                ?.takeIf { it != "전체" } ?: "Ping 개수",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { pingCountMenuExpanded = true }
                        )
                        DropdownMenu(
                            expanded = pingCountMenuExpanded,
                            onDismissRequest = { pingCountMenuExpanded = false }
                        ) {
                            pingCountOptions.forEach { (label, value) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        viewModel.onMinPingCountSelected(value)
                                        pingCountMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            selectedPlace?.let { place ->
                PlaceInfoCard(
                    place = place,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                )
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

@Composable
private fun RegionDropdownField(
    selectedRegion: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onRegionSelected: (String) -> Unit
) {
    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onExpandedChange(!expanded) }
        ) {
            Text(
                text = selectedRegion,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = Color.Black
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            // 상단에 "지역" 헤더를 위쪽 화살표와 함께 반복 표시 (다시 누르면 닫힘)
            DropdownMenuItem(
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("지역", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Filled.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                onClick = { onExpandedChange(false) }
            )

            regionOptions.forEach { region ->
                DropdownMenuItem(
                    text = {
                        Text(region, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    },
                    onClick = {
                        onRegionSelected(region)
                        onExpandedChange(false)
                    }
                )
            }
        }
    }
}