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

// 드롭다운에 보여줄 이름 -> (regionId, 지도 중심 좌표)
// regionId는 RouteMapEditScreen의 RegionMapCenters랑 값 맞춤 (R01~R04)
private val regionInfo: Map<String, Pair<String?, LatLng?>> = linkedMapOf(
    "전체" to (null to null),
    "서울" to ("R01" to LatLng(37.5665, 126.9780)),
    "경기" to ("R05" to LatLng(37.4138, 127.5183)),
    "강원" to ("R03" to LatLng(37.8228, 128.1555)),
    "인천" to ("R06" to LatLng(37.4563, 126.7052)),
    "대전" to ("R09" to LatLng(36.3504, 127.3845)),
    "세종" to ("R11" to LatLng(36.4801, 127.2890)),
    "충북" to ("R12" to LatLng(36.6357, 127.4917)),
    "충남" to ("R13" to LatLng(36.6588, 126.6728)),
    "부산" to ("R02" to LatLng(35.1796, 129.0756)),
    "대구" to ("R07" to LatLng(35.8714, 128.6014)),
    "울산" to ("R10" to LatLng(35.5384, 129.3114)),
    "경북" to ("R16" to LatLng(36.4919, 128.8889)),
    "경남" to ("R17" to LatLng(35.4606, 128.2132)),
    "전북" to ("R14" to LatLng(35.7175, 127.1530)),
    "전남" to ("R15" to LatLng(34.8161, 126.4629)),
    "광주" to ("R08" to LatLng(35.1595, 126.8526)),
    "제주" to ("R04" to LatLng(33.4996, 126.5312))
)
private val regionOptions = regionInfo.keys.toList()

private val timeSlotOptions = listOf("전체", "아침", "오전", "오후", "저녁", "밤/새벽")
private val pingCountOptions = listOf("전체" to null, "3개" to 3, "4개 이상" to 4, "5개 이상" to 5, "6개 이상" to 6)

private val AccentBlue = Color(0xFF0074CE)

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun PlaceSearchScreen(
    viewModel: PlaceSearchViewModel = viewModel(),
    onPlaceClick: (Long) -> Unit = {},
    onRouteClick: (Long) -> Unit = {}
) {
    val places by viewModel.places.collectAsState()
    val routes by viewModel.routes.collectAsState()
    val savedIndividualPlaces by viewModel.savedIndividualPlaces.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val selectedTimeSlot by viewModel.selectedTimeSlot.collectAsState()
    val selectedMinPingCount by viewModel.selectedMinPingCount.collectAsState()

    var regionMenuExpanded by remember { mutableStateOf(false) }
    var selectedRegionName by remember { mutableStateOf("지역") }
    var timeSlotMenuExpanded by remember { mutableStateOf(false) }
    var pingCountMenuExpanded by remember { mutableStateOf(false) }
    var selectedPlace by remember { mutableStateOf<PlaceSearchResponse?>(null) }

    val pinIcon = remember { OverlayImage.fromResource(R.drawable.blueping) }

    // 화면 처음 진입 시: 카테고리 미선택 상태이므로 전체 루트/핑 로드
    LaunchedEffect(Unit) { viewModel.loadSavedRoutesAndPlaces() }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {

        // ===== 상단 헤더 =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
                .background(Color.White)
        ) {
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

            // "지역" 드롭다운 — 선택하면 필터링 + 지도 카메라 이동까지 같이 처리
            Box(
                modifier = Modifier
                    .offset(x = 14.dp, y = 32.dp)
            ) {
                RegionDropdownField(
                    selectedRegion = selectedRegionName,
                    expanded = regionMenuExpanded,
                    onExpandedChange = { regionMenuExpanded = it },
                    onRegionSelected = { regionName ->
                        selectedRegionName = regionName
                        val (regionId, _) = regionInfo[regionName] ?: (null to null)
                        viewModel.onRegionSelected(regionId)
                    }
                )
            }

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
                        color = if (isSelected) AccentBlue else Color(0xFF818181),
                        modifier = Modifier.clickable { viewModel.onCategorySelected(category) }
                    )
                }
            }
        }

        // ===== 지도 영역 =====
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {

            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition(LatLng(37.5665, 126.9780), 12.0)
            }

            // 지역 선택이 바뀔 때마다 그 지역 중심으로 카메라 이동 ("전체"면 안 움직임)
            LaunchedEffect(selectedRegionName) {
                val (_, center) = regionInfo[selectedRegionName] ?: (null to null)
                if (center != null) {
                    cameraPositionState.position = CameraPosition(center, 12.0)
                }
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
                            color = AccentBlue,
                            width = 8.dp,
                            onClick = {
                                onRouteClick(route.routeId)
                                true
                            }
                        )
                    }
                }

                if (selectedCategory == null) {
                    // 카테고리 미선택: 내가 저장한 루트에 속한 장소들 + 개별로 저장한 장소들을 핑으로 표시
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

                    val routeSpotIds = routePlaces.map { it.spotId }.toSet()
                    savedIndividualPlaces
                        .filter { it.spotId !in routeSpotIds } // 루트에 이미 포함된 장소는 중복 표시 안 함
                        .forEach { place ->
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