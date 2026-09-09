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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
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
    var showSuccessDialog by remember { mutableStateOf(false) } // 등록 완료 안내창 노출 여부
    var registeredPlace by remember { mutableStateOf<PlaceSearchResponse?>(null) } // 방금 등록 완료된 장소 (확인 누르면 다음 단계로 넘김)

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

                // 1단계: 신규 장소 카테고리 선택창
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
                                        registeredPlace = created
                                        showSuccessDialog = true
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

                // 2단계: 등록 완료 안내창 - "확인" 누르면 그제서야 다음 단계(핑 등록)로 넘어감
                if (showSuccessDialog) {
                    registeredPlace?.let { place ->
                        PlaceRegisteredDialog(
                            onConfirm = {
                                showSuccessDialog = false
                                onPlaceSelected(place)
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

// 신규(미등록) 장소 등록 시 카테고리를 아이콘으로 직접 고르는 선택창
@Composable
private fun CategoryPickerDialog(
    onDismiss: () -> Unit,
    onCategorySelected: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "이 장소를 등록하시겠습니까?",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                Text(
                    text = "×",
                    fontSize = 18.sp,
                    color = GrayText,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable { onDismiss() }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "이 장소는 어떤 장소인가요?", fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                CategoryIconOption(
                    iconRes = R.drawable.fork_spoon,
                    label = "맛집",
                    onClick = { onCategorySelected("맛집") }
                )
                CategoryIconOption(
                    iconRes = R.drawable.camera,
                    label = "관광지",
                    onClick = { onCategorySelected("관광지") }
                )
                CategoryIconOption(
                    iconRes = R.drawable.coffee,
                    label = "카페",
                    onClick = { onCategorySelected("카페") }
                )
            }
        }
    }
}

@Composable
private fun CategoryIconOption(iconRes: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(GrayBg),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = label,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

// 장소 등록 완료 안내창 - 마스코트 + "확인" 버튼
@Composable
private fun PlaceRegisteredDialog(onConfirm: () -> Unit) {
    Dialog(onDismissRequest = onConfirm) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "장소가 등록되었습니다!", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Image(
                painter = painterResource(id = R.drawable.check_dino),
                contentDescription = "등록 완료",
                modifier = Modifier.size(120.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BluePrimary)
                    .clickable { onConfirm() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "확인", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

// ===== 프리뷰 =====
// SelectedPlaceCard: 정적 프리뷰에서 바로 잘 보임
@Preview(showBackground = true)
@Composable
private fun SelectedPlaceCardRegisteredPreview() {
    SelectedPlaceCard(
        place = PlaceSearchResponse(
            spotId = 1L,
            name = "서울 암사동 유적",
            category = "관광지",
            address = "",
            latitude = 37.55,
            longitude = 127.13,
            imageUrl = null,
            description = null
        ),
        isRegistered = true,
        popularTimeSlot = "오후 2시 ~ 4시",
        totalPingCount = 2L,
        isRegistering = false,
        onAddClick = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun SelectedPlaceCardUnregisteredPreview() {
    SelectedPlaceCard(
        place = PlaceSearchResponse(
            spotId = UNREGISTERED_SPOT_ID,
            name = "Camel Coffee",
            category = "장소",
            address = "",
            latitude = 37.55,
            longitude = 127.13,
            imageUrl = null,
            description = null
        ),
        isRegistered = false,
        popularTimeSlot = null,
        totalPingCount = null,
        isRegistering = false,
        onAddClick = {}
    )
}

// CategoryPickerDialog / PlaceRegisteredDialog: Dialog로 감싸져 있어서 정적 프리뷰에서
// 빈 화면으로 보일 수 있음 -> Android Studio에서 "Interactive Preview"(재생 버튼)로 실행해서 확인할 것
@Preview(showBackground = true)
@Composable
private fun CategoryPickerDialogPreview() {
    CategoryPickerDialog(
        onDismiss = {},
        onCategorySelected = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun PlaceRegisteredDialogPreview() {
    PlaceRegisteredDialog(onConfirm = {})
}