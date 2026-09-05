// [파일 설명] Ping "코스 상세" 화면의 "+" 버튼 전용 장소 선택 화면. 지도에서 장소를 골라 그 코스에 새 핑을 등록할 때 씀 (일반 장소 검색인 PlaceSearchScreen과는 별개).
package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.overlay.OverlayImage
import com.tripping.app.R
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.request.CreatePlaceRequest
import com.tripping.app.data.response.PlaceSearchResponse
import com.tripping.app.viewmodel.PlaceCategory
import com.tripping.app.viewmodel.PlaceSearchViewModel
import kotlinx.coroutines.launch

// 대한민국 17개 시/도 (특별시/광역시/도/특별자치도) - regionId는 백엔드 실제 코드 확정되면 매핑 필요
private val regionOptions = listOf(
    "전체",
    "서울", "부산", "대구", "인천", "광주", "대전", "울산", "세종",
    "경기", "강원", "충북", "충남", "전북", "전남", "경북", "경남",
    "제주"
)
private val BluePrimary = Color(0xFF4A72C4)
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)

// 아직 우리 DB에 등록 안 된 (네이버맵에서 방금 탭한) 장소를 나타내는 sentinel 값
private const val UNREGISTERED_SPOT_ID = -1L

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun PingPlaceSearchScreen(
    viewModel: PlaceSearchViewModel = viewModel(),
    onPlaceSelected: (PlaceSearchResponse) -> Unit
) {
    val places by viewModel.places.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var selectedPlace by remember { mutableStateOf<PlaceSearchResponse?>(null) }

    var regionMenuExpanded by remember { mutableStateOf(false) }
    var selectedRegion by remember { mutableStateOf("지역") }
    var isRegistering by remember { mutableStateOf(false) } // "+" 눌러서 신규 장소 등록 중일 때 버튼 비활성화용
    var showCategoryPicker by remember { mutableStateOf(false) } // 신규 장소 등록 전, 카테고리 선택창 노출 여부

    val pinIcon = remember { OverlayImage.fromResource(R.drawable.ic_map_pin) }
    val coroutineScope = rememberCoroutineScope()

    // 선택된 장소의 핑 통계(인기 시간대 / 핑 개수) - DB에 등록된 장소일 때만 조회
    var popularTimeSlot by remember { mutableStateOf<String?>(null) }
    var totalPingCount by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) { viewModel.loadPlaces() }

    LaunchedEffect(selectedPlace) {
        val place = selectedPlace
        if (place == null || place.spotId == UNREGISTERED_SPOT_ID) {
            // 신규(미등록) 장소는 spotId가 없어서 통계 조회 자체가 불가능함
            popularTimeSlot = null
            totalPingCount = null
            return@LaunchedEffect
        }
        try {
            val stats = RetrofitClient.pingApi.getSpotPingStats(place.spotId)
            popularTimeSlot = stats.popularTimeSlot
            totalPingCount = stats.totalPingCount
        } catch (e: Exception) {
            popularTimeSlot = null
            totalPingCount = null
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ===== 상단 바 =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 20.dp, start = 15.dp, end = 15.dp, bottom = 12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { regionMenuExpanded = true }
                        ) {
                            Text(
                                text = selectedRegion,
                                fontSize = 22.sp,
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

                    Text(
                        text = "동네핑거가 등록한",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF818181)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

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

            // ===== 지도 =====
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition(LatLng(37.5665, 126.9780), 12.0)
                }

                NaverMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { _, _ -> selectedPlace = null },
                    // 👈 네이버 지도 자체에 원래 있는 POI(카페/식당 아이콘 등)를 탭했을 때
                    // 우리 DB에 없는 장소이므로 spotId를 임시로 UNREGISTERED로 표시
                    onSymbolClick = { symbol ->
                        selectedPlace = PlaceSearchResponse(
                            spotId = UNREGISTERED_SPOT_ID,
                            name = symbol.caption.ifBlank { "이름 없는 장소" },
                            category = "장소", // TODO: 네이버 SDK가 카테고리를 직접 안 줘서 임시값. 필요하면 별도 API로 보강
                            address = "",
                            latitude = symbol.position.latitude,
                            longitude = symbol.position.longitude,
                            imageUrl = null,
                            description = null
                        )
                        true
                    }
                ) {
                    // 우리 DB에 이미 등록된 장소들 (커스텀 마커)
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

                selectedPlace?.let { place ->
                    val isRegistered = place.spotId != UNREGISTERED_SPOT_ID

                    SelectedPlaceCard(
                        place = place,
                        isRegistered = isRegistered,
                        popularTimeSlot = popularTimeSlot,
                        totalPingCount = totalPingCount,
                        isRegistering = isRegistering,
                        onAddClick = {
                            if (isRegistered) {
                                // 이미 DB에 있는 장소 -> 바로 핑 등록 진행
                                onPlaceSelected(place)
                            } else {
                                // DB에 없는 장소 -> 카테고리부터 물어봄 (네이버 지도 SDK가 카테고리를 안 줘서
                                // 자동 판별이 안 되므로, 배포 일정상 사용자가 직접 고르게 함)
                                showCategoryPicker = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                    )
                }

                // 신규 장소 카테고리 선택창 - 고른 카테고리로 등록 -> 핑 등록까지 이어감
                if (showCategoryPicker) {
                    selectedPlace?.let { place ->
                        CategoryPickerDialog(
                            onDismiss = { showCategoryPicker = false },
                            onCategorySelected = { categoryLabel ->
                                showCategoryPicker = false
                                isRegistering = true
                                coroutineScope.launch {
                                    try {
                                        val created = RetrofitClient.placeApi.createPlace(
                                            CreatePlaceRequest(
                                                name = place.name,
                                                category = categoryLabel,
                                                latitude = place.latitude,
                                                longitude = place.longitude
                                            )
                                        )
                                        onPlaceSelected(created)
                                    } catch (e: Exception) {
                                        // TODO: 등록 실패 시 에러 안내 (스낵바 등)
                                    } finally {
                                        isRegistering = false
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedPlaceCard(
    place: PlaceSearchResponse,
    isRegistered: Boolean,
    popularTimeSlot: String?,
    totalPingCount: Long?,
    isRegistering: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // 이름 + 카테고리 칩 + "+" 추가 버튼
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = place.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Box(
                modifier = Modifier
                    .background(GrayBg, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(text = place.category, fontSize = 11.sp, color = GrayText)
            }
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .size(40.dp) // 터치 영역은 넉넉하게 (실제 아이콘보다 크게 잡아서 탭하기 쉽게)
                    .clickable(enabled = !isRegistering) { onAddClick() },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.pingplus),
                    contentDescription = "핑 추가",
                    modifier = Modifier.size(28.dp),
                    alpha = if (isRegistering) 0.4f else 1f
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // "가장 많이 가는 시간" 행 (시계 아이콘 + 라벨/값)
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(id = R.drawable.clock),
                contentDescription = null,
                modifier = Modifier.size(15.dp).padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "가장 많이 가는 시간", fontSize = 12.sp, color = GrayText)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isRegistered) (popularTimeSlot ?: "아직 등록되지 않았어요") else "아직 등록되지 않았어요",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider(color = Color(0xFFECECEC), thickness = 1.dp)
        Spacer(modifier = Modifier.height(10.dp))

        // "핑 개수" 행 (핀 아이콘 + 라벨/값)
        Row(verticalAlignment = Alignment.Top) {
            Image(
                painter = painterResource(id = R.drawable.ping),
                contentDescription = null,
                modifier = Modifier.size(15.dp).padding(top = 1.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = "핑 개수", fontSize = 12.sp, color = GrayText)
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (isRegistered) "${totalPingCount ?: 0}" else "아직 등록되지 않았어요",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// 신규(미등록) 장소 등록 시 카테고리를 직접 고르는 선택창
@Composable
private fun CategoryPickerDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "카테고리를 선택해주세요", fontWeight = FontWeight.Bold) },
        text = {
            Column {
                PlaceCategory.entries.forEach { category ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onCategorySelected(category.label) }
                            .padding(vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = category.label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "취소", color = GrayText)
            }
        }
    )
}