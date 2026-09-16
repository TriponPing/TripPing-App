package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.tripping.app.R
import com.tripping.app.data.api.RetrofitClient
import com.tripping.app.data.request.CreatePlaceRequest
import com.tripping.app.data.response.PlaceSearchResponse
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

private val AccentBlue = Color(0xFF0074CE)
private val GrayText = Color(0xFF818181)
private val LightGrayBorder = Color(0xFFE0E0E0)
private val GrayBg = Color(0xFFF3F3F5)

/**
 * 지역 코드(regionId, 예: "R01") -> 초기 지도 중심 좌표.
 */
private object RegionMapCenters {
    private val centers = mapOf(
        "R01" to LatLng(37.5665, 126.9780), // 서울
        "R02" to LatLng(35.1796, 129.0756), // 부산
        "R03" to LatLng(37.8228, 128.1555), // 강원
        "R04" to LatLng(33.4996, 126.5312)  // 제주
    )

    fun findCenter(regionId: String): LatLng {
        return centers[regionId] ?: centers.getValue("R01")
    }
}

// 지도에서 방금 탭한(아직 우리 DB에 등록됐는지 모르는) 장소 정보
private data class TappedMapPlace(
    val name: String,
    val latitude: Double,
    val longitude: Double
)

/**
 * "추천 안 받고 다음으로" / "적용 후 편집" 눌렀을 때 보여주는 화면.
 * 지도를 탭해서 장소를 추가하고 (우리 DB에 없는 곳이면 카테고리 골라서 새로 등록),
 * 하단 리스트에서 꾹 눌러 순서를 바꾸고, 저장해놓은 장소를 지도 위 버튼으로 불러와서 추가할 수 있어요.
 *
 * @param initialRegionText 지도 초기 중심 계산용 지역 코드 (예: "R01") — RouteCreateViewModel의 lastRegionId
 * @param routePlaces 현재 루트에 담긴 장소 목록 (order 순서대로)
 * @param onRoutePlacesChange 순서 변경/삭제 등으로 목록이 바뀔 때마다 호출
 * @param onAddPlace 저장한 장소 목록 또는 지도에서 새로 등록한 장소를 "추가"할 때
 * @param savedPlaces 사용자가 저장해놓은 장소 목록
 * @param onNextClick 이 화면에서 "다음"으로 넘어갈 때 (사용자가 입력한 루트 제목을 같이 넘김)
 */
@Composable
fun RouteMapEditScreen(
    initialRegionText: String,
    routePlaces: List<RoutePlaceItem>,
    onRoutePlacesChange: (List<RoutePlaceItem>) -> Unit,
    onAddPlace: (RoutePlaceItem) -> Unit,
    savedPlaces: List<RoutePlaceItem>,
    onBackClick: () -> Unit = {},
    onNextClick: (String) -> Unit = {}
) {
    var routeTitle by remember { mutableStateOf("") }
    var showSavedPlacesPanel by remember { mutableStateOf(false) }
    val initialCenter = remember(initialRegionText) { RegionMapCenters.findCenter(initialRegionText) }

    // ===== 지도 탭해서 새 장소 등록하는 흐름용 상태 =====
    var tappedPlace by remember { mutableStateOf<TappedMapPlace?>(null) }
    var isRegistering by remember { mutableStateOf(false) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var registeredPlace by remember { mutableStateOf<PlaceSearchResponse?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        // ===== 상단 타이틀 + 뒤로가기 =====
        Text(
            text = "루트",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
        )
        BackButtonRow(onClick = onBackClick)
        HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)

        RouteStepIndicator(currentStep = 2)

        // ===== 루트 제목 입력 =====
        Column(modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp)) {
            Text(text = "루트 제목", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "루트를 기억하기 쉬운 이름을 입력해주세요", fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = routeTitle,
                onValueChange = { routeTitle = it },
                placeholder = { Text("예: 제주도 힐링 여행", fontSize = 14.sp, color = GrayText) },
                singleLine = true,
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = GrayBg,
                    focusedContainerColor = GrayBg,
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = AccentBlue
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ===== 지도 + 저장한 장소 버튼 + 탭한 장소 카드 =====
        Box(modifier = Modifier.weight(1f)) {
            NaverEditableMapView(
                modifier = Modifier.fillMaxSize(),
                initialCenter = initialCenter,
                places = routePlaces,
                onSymbolTapped = { name, lat, lng ->
                    tappedPlace = TappedMapPlace(name, lat, lng)
                },
                onMapBackgroundTapped = {
                    tappedPlace = null
                }
            )

            FloatingCircleButton(
                icon = Icons.Filled.Bookmark,
                contentDescription = "저장한 장소",
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                onClick = { showSavedPlacesPanel = !showSavedPlacesPanel }
            )

            if (showSavedPlacesPanel) {
                SavedPlacesOverlayPanel(
                    savedPlaces = savedPlaces,
                    onAddPlace = { place ->
                        onAddPlace(place)
                        showSavedPlacesPanel = false
                    },
                    onDismiss = { showSavedPlacesPanel = false }
                )
            }

            tappedPlace?.let { tapped ->
                TappedPlaceCard(
                    name = tapped.name,
                    isRegistering = isRegistering,
                    onAddClick = { showCategoryPicker = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
                )
            }

            // 1단계: 카테고리 선택창
            if (showCategoryPicker) {
                tappedPlace?.let { tapped ->
                    CategoryPickerDialog(
                        onDismiss = { showCategoryPicker = false },
                        onCategorySelected = { categoryLabel ->
                            showCategoryPicker = false
                            isRegistering = true
                            coroutineScope.launch {
                                try {
                                    val created = RetrofitClient.placeApi.createPlace(
                                        CreatePlaceRequest(
                                            name = tapped.name,
                                            category = categoryLabel,
                                            latitude = tapped.latitude,
                                            longitude = tapped.longitude
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

            // 2단계: 등록 완료 안내창 - "확인" 누르면 루트 리스트에 추가됨
            if (showSuccessDialog) {
                registeredPlace?.let { place ->
                    PlaceRegisteredDialog(
                        onConfirm = {
                            showSuccessDialog = false
                            onAddPlace(
                                RoutePlaceItem(
                                    order = 0,
                                    name = place.name,
                                    tags = listOf(place.category),
                                    latitude = place.latitude,
                                    longitude = place.longitude,
                                    spotId = place.spotId
                                )
                            )
                            tappedPlace = null
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp)) // 지도랑 리스트 사이 간격

        // ===== 하단 루트 리스트 (비어있으면 안내 문구, 있으면 이름 + X 리스트) =====
        if (routePlaces.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "지도를 클릭해서 장소를 추가하세요",
                    fontSize = 14.sp,
                    color = GrayText
                )
            }
        } else {
            ReorderablePlaceList(
                places = routePlaces,
                onReorder = onRoutePlacesChange,
                onRemove = { place ->
                    val updated = (routePlaces - place).mapIndexed { index, p -> p.copy(order = index + 1) }
                    onRoutePlacesChange(updated)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 260.dp)
            )
        }

        Button(
            onClick = {
                val finalTitle = routeTitle.ifBlank { "내가 만든 루트" }
                onNextClick(finalTitle)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text(text = "다음", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun RouteStepIndicator(currentStep: Int) {
    val steps = listOf("정보 입력", "추천 확인", "완료")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isDone = stepNumber < currentStep

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isActive || isDone) AccentBlue else Color(0xFFE0E0E0),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stepNumber.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) AccentBlue else GrayText
                )
            }
            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f).padding(bottom = 20.dp),
                    color = if (stepNumber < currentStep) AccentBlue else Color(0xFFE0E0E0),
                    thickness = 2.dp
                )
            }
        }
    }
}

@Composable
private fun NaverEditableMapView(
    modifier: Modifier = Modifier,
    initialCenter: LatLng,
    places: List<RoutePlaceItem>,
    onSymbolTapped: (name: String, latitude: Double, longitude: Double) -> Unit,
    onMapBackgroundTapped: () -> Unit
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val markers = remember { mutableStateListOf<Marker>() }
    val pathOverlayState = remember { mutableStateOf<PathOverlay?>(null) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    var didSetInitialCamera by remember { mutableStateOf(false) }
    var lastAppliedSignature by remember { mutableStateOf<String?>(null) }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.getMapAsync { naverMap ->
                // 👈 수정: locale 지정 안 하면 기기 시스템 언어를 따라가서 지도 위 장소 이름(POI)이
                // 영어로 나올 수 있음 - 한국어로 고정.
                naverMap.locale = Locale.KOREA
                redrawMarkers(naverMap, places, markers, pathOverlayState)

                // 네이버 지도 자체 POI(카페/식당 아이콘 등) 탭 -> 아직 우리 DB에 없는 장소로 취급
                naverMap.setOnSymbolClickListener { symbol ->
                    onSymbolTapped(
                        symbol.caption.ifBlank { "이름 없는 장소" },
                        symbol.position.latitude,
                        symbol.position.longitude
                    )
                    true
                }

                naverMap.setOnMapClickListener { _, _ ->
                    onMapBackgroundTapped()
                }

                // 장소 목록이 "진짜로 바뀔 때"만 카메라 이동 (안 그러면 recompose 될 때마다
                // 카메라가 강제로 재정렬돼서, 사용자가 지도를 자유롭게 못 움직이게 됨)
                val validPlaces = places.filter { it.latitude != null && it.longitude != null }
                val signature = validPlaces.joinToString(",") { "${it.latitude}_${it.longitude}" }

                if (signature != lastAppliedSignature) {
                    lastAppliedSignature = signature

                    if (validPlaces.isEmpty()) {
                        // 아직 추가한 장소가 없으면 선택 지역 중심으로 (최초 1번만)
                        if (!didSetInitialCamera) {
                            naverMap.moveCamera(CameraUpdate.scrollTo(initialCenter))
                            didSetInitialCamera = true
                        }
                    } else {
                        // 장소가 실제로 추가/삭제/순서변경 됐을 때만 전체 루트가 다 보이도록 맞춤
                        val latLngs = validPlaces.map { LatLng(it.latitude!!, it.longitude!!) }
                        val cameraUpdate = if (latLngs.size == 1) {
                            CameraUpdate.scrollTo(latLngs.first())
                        } else {
                            val bounds = LatLngBounds.Builder().apply {
                                latLngs.forEach { include(it) }
                            }.build()
                            CameraUpdate.fitBounds(bounds, 120)
                        }
                        naverMap.moveCamera(cameraUpdate)
                    }
                }
            }
        }
    )
}

private fun redrawMarkers(
    naverMap: NaverMap,
    places: List<RoutePlaceItem>,
    markers: MutableList<Marker>,
    pathOverlayState: MutableState<PathOverlay?>
) {
    // 기존에 그려둔 마커/경로선을 먼저 지움 (안 지우면 추가/삭제/순서변경 할 때마다 겹쳐서 쌓임)
    markers.forEach { it.map = null }
    markers.clear()
    pathOverlayState.value?.map = null
    pathOverlayState.value = null

    val validPlaces = places
        .sortedBy { it.order }
        .filter { it.latitude != null && it.longitude != null }

    if (validPlaces.isEmpty()) return

    val latLngs = validPlaces.map { LatLng(it.latitude!!, it.longitude!!) }
    val pinIcon = OverlayImage.fromResource(R.drawable.map_ping)

    latLngs.forEach { latLng ->
        val marker = Marker().apply {
            position = latLng
            icon = pinIcon
            map = naverMap
        }
        markers.add(marker)
    }

    if (latLngs.size >= 2) {
        // 경로선 색을 마커(map_ping) 색과 통일
        val pinLineColor = 0xFF0074CE.toInt()
        val newPath = PathOverlay().apply {
            coords = latLngs
            color = pinLineColor
            outlineColor = pinLineColor
            map = naverMap
        }
        pathOverlayState.value = newPath
    }
}

@Composable
private fun FloatingCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(4.dp, CircleShape)
            .background(AccentBlue, CircleShape)
            .clickable { onClick() }
            .padding(14.dp)
    ) {
        Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(22.dp))
    }
}

// 지도에서 방금 탭한 장소 이름 + "추가" 버튼 카드
@Composable
private fun TappedPlaceCard(
    name: String,
    isRegistering: Boolean,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .background(AccentBlue, RoundedCornerShape(20.dp))
                .clickable(enabled = !isRegistering) { onAddClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isRegistering) "등록 중..." else "+ 추가",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
        }
    }
}

// 신규(미등록) 장소 등록 시 카테고리를 아이콘으로 직접 고르는 선택창
// (PingPlaceSearchScreen.kt의 CategoryPickerDialog와 동일 — private이라 재사용이 안 돼서 복제함)
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
// (PingPlaceSearchScreen.kt의 PlaceRegisteredDialog와 동일 — private이라 재사용이 안 돼서 복제함)
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
                    .background(AccentBlue)
                    .clickable { onConfirm() },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "확인", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
private fun SavedPlacesOverlayPanel(
    savedPlaces: List<RoutePlaceItem>,
    onAddPlace: (RoutePlaceItem) -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(6.dp, RoundedCornerShape(16.dp))
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "저장한 장소", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "닫기")
            }
        }

        if (savedPlaces.isEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "저장해놓은 장소가 없어요", fontSize = 13.sp, color = GrayText)
        } else {
            Spacer(modifier = Modifier.height(4.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 280.dp)) {
                itemsIndexed(savedPlaces) { _, place ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddPlace(place) }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Bookmark, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = place.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                            if (place.tags.isNotEmpty()) {
                                Text(text = place.tags.joinToString(" · "), fontSize = 12.sp, color = GrayText)
                            }
                        }
                        Text(text = "+ 추가", fontSize = 13.sp, color = AccentBlue, fontWeight = FontWeight.SemiBold)
                    }
                    HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)
                }
            }
        }
    }
}

/**
 * 이름 + X 로 구성된 라운드 박스 리스트. 꾹 눌러서 드래그하면 순서가 바뀌어요.
 * 외부 라이브러리 없이 직접 구현한 간단 버전이라, 아이템이 아주 많거나
 * 더 매끄러운 애니메이션이 필요하면 org.burnoutcrew.compose-reorderable로 교체를 추천해요.
 */
@Composable
private fun ReorderablePlaceList(
    places: List<RoutePlaceItem>,
    onReorder: (List<RoutePlaceItem>) -> Unit,
    onRemove: (RoutePlaceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggingIndex by remember { mutableStateOf(-1) }
    var dragOffsetY by remember { mutableStateOf(0f) }
    val rowHeightPx = remember { mutableStateOf(140f) } // 대략적인 행 높이(px), 필요시 onGloballyPositioned로 정교화 가능

    LazyColumn(
        modifier = modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(places, key = { _, place -> "${place.order}-${place.name}" }) { index, place ->
            val isDragging = index == draggingIndex
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { translationY = if (isDragging) dragOffsetY else 0f }
                    .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
                    .background(if (isDragging) Color(0xFFF5F5F5) else Color.White, RoundedCornerShape(12.dp))
                    .pointerInput(places) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggingIndex = index
                                dragOffsetY = 0f
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffsetY += dragAmount.y

                                val rowHeight = rowHeightPx.value
                                val moveBy = (dragOffsetY / rowHeight).roundToInt()
                                if (moveBy != 0) {
                                    val targetIndex = (draggingIndex + moveBy).coerceIn(0, places.size - 1)
                                    if (targetIndex != draggingIndex) {
                                        val mutable = places.toMutableList()
                                        val moved = mutable.removeAt(draggingIndex)
                                        mutable.add(targetIndex, moved)
                                        // 순서가 바뀐 만큼 order 값도 다시 매겨서, 지도 연결선이 새 순서로 그려지게 함
                                        val renumbered = mutable.mapIndexed { i, p -> p.copy(order = i + 1) }
                                        onReorder(renumbered)
                                        draggingIndex = targetIndex
                                        dragOffsetY -= moveBy * rowHeight
                                    }
                                }
                            },
                            onDragEnd = { draggingIndex = -1; dragOffsetY = 0f },
                            onDragCancel = { draggingIndex = -1; dragOffsetY = 0f }
                        )
                    }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = place.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    Icons.Filled.Close,
                    contentDescription = "삭제",
                    tint = GrayText,
                    modifier = Modifier
                        .size(18.dp)
                        .clickable { onRemove(place) }
                )
            }
        }
    }
}

@Composable
private fun BackButtonRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowLeft,
            contentDescription = "뒤로가기",
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = "뒤로가기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}
