package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.tripping.app.R
import com.tripping.app.data.response.MapPinResponse
import com.tripping.app.data.response.SavedRouteResponse
import com.tripping.app.data.response.TripSummaryResponse
import com.tripping.app.ui.component.NaverMapContainer
import com.tripping.app.ui.component.applyPingIcon
import com.tripping.app.ui.component.cameraUpdateToShowAll
import com.tripping.app.viewmodel.MyPageViewModel

// ===== 마이페이지 화면 - Figma 디자인 기준 =====
// 실제 API(GET /users/me, /users/me/trips/recent, /users/me/routes/saved, /users/me/map) 연동됨.
// TODO: 뱃지(여행 작성자/연속 출석)는 백엔드에 아직 관련 API가 없어서 임시 고정값으로 둠.

private val ColorBackground = Color(0xFFF8F8FC)
private val ColorAccentBlue = Color(0xFF0074CE)
private val ColorLevelChipBg = Color(0xFFD9D9D9)

// ===== 네트워크 응답 -> 화면용 카드 모델 변환 =====

private fun TripSummaryResponse.toCardModel(): MyPageTripCard = MyPageTripCard(
    title = representativeSpotName ?: "여행 기록",
    dateRange = travelDate,
    pingCount = placeCount,
    routeId = tripId
)

private fun SavedRouteResponse.toCardModel(): MyPageTripCard = MyPageTripCard(
    title = representativeSpotName ?: "여행 기록",
    dateRange = travelDate,
    pingCount = placeCount,
    isSaved = true,
    routeId = tripId
)

@Composable
fun MyPageScreen(
    onOpenSettings: () -> Unit = {},
    onOpenTripHistory: () -> Unit = {},
    onOpenMyMap: () -> Unit = {},
    onOpenTripDetail: (Long, String) -> Unit = { _, _ -> },
    viewModel: MyPageViewModel = viewModel()
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = 여행기록, 1 = 저장한 여행

    LaunchedEffect(Unit) {
        viewModel.loadAll()
    }

    val profile by viewModel.profile.collectAsState()
    val recentTrips by viewModel.recentTrips.collectAsState()
    val savedRoutes by viewModel.savedRoutes.collectAsState()
    val savedRoutesTotal by viewModel.savedRoutesTotal.collectAsState()
    val visitedPlaceCount by viewModel.visitedPlaceCount.collectAsState()
    val mapPins by viewModel.mapPins.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        MyPageTopBar(onOpenSettings = onOpenSettings)
        MyPageProfileSection(nickname = profile?.nickname, level = profile?.level)
        MyPageTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it })

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> TripRecordTab(
                    trips = recentTrips.map { it.toCardModel() },
                    visitedPlaceCount = visitedPlaceCount,
                    mapPins = mapPins,
                    onOpenTripHistory = onOpenTripHistory,
                    onOpenMyMap = onOpenMyMap,
                    onOpenTripDetail = onOpenTripDetail
                )
                else -> SavedTripTab(
                    savedTrips = savedRoutes.map { it.toCardModel() },
                    total = savedRoutesTotal,
                    onCancelSave = { routeId -> viewModel.unsaveRoute(routeId) },
                    onOpenTripDetail = onOpenTripDetail
                )
            }
        }
    }
}

// ===== 상단바 =====
@Composable
private fun MyPageTopBar(onOpenSettings: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp)
            .height(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "마이페이지",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Image(
            painter = painterResource(id = R.drawable.settings),
            contentDescription = "설정",
            modifier = Modifier
                .size(24.dp)
                .clickable { onOpenSettings() }
        )
    }
}

// ===== 프로필 영역 (아바타 + 이름 + 레벨 + 뱃지) =====
@Composable
private fun MyPageProfileSection(nickname: String?, level: String?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 아바타 (TODO: 실제 프로필 이미지로 교체 - GET /users/me의 profileImage)
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8EEF5)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "👤", fontSize = 28.sp)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = nickname ?: "불러오는 중...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(11.dp))
                    .background(ColorLevelChipBg)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(text = if (level != null) "Lv.$level" else "-", fontSize = 13.sp, color = ColorTextPrimary)
            }
        }

        // 뱃지 2개 (TODO: 실제 뱃지 아이콘/데이터로 교체 - 백엔드에 아직 관련 API 없음)
        BadgeCircle(emoji = "📷", label = "여행 작성자")
        Spacer(modifier = Modifier.width(8.dp))
        BadgeCircle(emoji = "📅", label = "연속 출석")
    }
}

@Composable
private fun BadgeCircle(emoji: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEFF3F8)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = emoji, fontSize = 18.sp)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = label, fontSize = 9.sp, color = ColorTextSecondary)
    }
}

// ===== 탭 (여행기록 / 저장한 여행) =====
@Composable
private fun MyPageTabRow(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val tabs = listOf("여행기록", "저장한 여행")

    Column(modifier = Modifier.background(Color.White)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, title ->
                val isSelected = selectedTab == index
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onTabSelected(index) }
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = title,
                        fontSize = 16.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) ColorAccentBlue else ColorTextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth(0.5f)
                            .background(if (isSelected) ColorAccentBlue else Color.Transparent)
                    )
                }
            }
        }
    }
}

// ===== 탭1: 여행기록 (다녀온 여행 + 나의 여행 지도) =====
@Composable
private fun TripRecordTab(
    trips: List<MyPageTripCard>,
    visitedPlaceCount: Int,
    mapPins: List<MapPinResponse>,
    onOpenTripHistory: () -> Unit,
    onOpenMyMap: () -> Unit,
    onOpenTripDetail: (Long, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            Column {
                SectionHeader(title = "다녀온 여행", actionLabel = "자세히 보기", onActionClick = onOpenTripHistory)
                Spacer(modifier = Modifier.height(12.dp))
                if (trips.isEmpty()) {
                    EmptyStateText("아직 다녀온 여행이 없어요")
                } else {
                    trips.forEach { trip ->
                        TripCard(
                            trip = trip,
                            onClick = { trip.routeId?.let { onOpenTripDetail(it, trip.title) } }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // 나머지 화면을 꽉 채우도록 fillParentMaxHeight 사용 (LazyColumn 뷰포트 기준 비율)
        item {
            Column(modifier = Modifier.fillParentMaxHeight(0.55f)) {
                SectionHeader(title = "나의 여행 지도", actionLabel = "자세히 보기", onActionClick = onOpenMyMap)
                Spacer(modifier = Modifier.height(12.dp))
                MyMapPreviewCard(
                    pins = mapPins,
                    visitedPlaceCount = visitedPlaceCount,
                    onClick = onOpenMyMap,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ===== 탭2: 저장한 여행 =====
@Composable
private fun SavedTripTab(
    savedTrips: List<MyPageTripCard>,
    total: Int,
    onCancelSave: (Long) -> Unit,
    onOpenTripDetail: (Long, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "저장한 여행을 눌러서 상세보기, 오른쪽 아이콘 눌러 저장 취소",
                fontSize = 11.sp,
                color = ColorTextSecondary,
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = ColorTextPrimary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = total.toString(),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ColorTextPrimary
            )
        }

        if (savedTrips.isEmpty()) {
            EmptyStateText("저장한 여행이 없어요")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedTrips) { trip ->
                    SavedTripCard(
                        trip = trip,
                        onClick = { trip.routeId?.let { onOpenTripDetail(it, trip.title) } },
                        onCancelSave = {
                            trip.routeId?.let(onCancelSave)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionLabel: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = ColorTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "$actionLabel ›",
            fontSize = 12.sp,
            color = ColorTextSecondary,
            modifier = Modifier.clickable { onActionClick() }
        )
    }
}

@Composable
private fun SavedTripCard(trip: MyPageTripCard, onClick: () -> Unit, onCancelSave: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, ColorCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = trip.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
                Spacer(modifier = Modifier.height(6.dp))
                Text(text = trip.dateRange, fontSize = 12.sp, color = ColorTextSecondary)
            }
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = "저장 취소",
                tint = ColorTextPrimary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onCancelSave() }
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoutePreviewDots(modifier = Modifier.weight(1f), count = trip.pingCount ?: 4)
            if (trip.pingCount != null || trip.duration != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    trip.pingCount?.let {
                        Text(text = "핑 ${it}개", fontSize = 12.sp, color = ColorTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    trip.duration?.let {
                        Text(text = it, fontSize = 12.sp, color = ColorTextSecondary)
                    }
                }
            }
        }
    }
}

// 마이페이지용 지도 미리보기 - 실제 네이버 지도에 핀만 찍어서 보여줌 (자세히보기 화면과 같은 데이터, GET /users/me/map).
// 확대/축소/이동 제스처는 막아두고(미리보기 용도), 지도를 탭하면 자세히보기 화면으로 이동함.
@Composable
private fun MyMapPreviewCard(
    pins: List<MapPinResponse>,
    visitedPlaceCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8EEF5))
    ) {
        NaverMapContainer(
            modifier = Modifier.fillMaxSize(),
            onMapReady = { map ->
                map.uiSettings.apply {
                    isScrollGesturesEnabled = false
                    isZoomGesturesEnabled = false
                    isRotateGesturesEnabled = false
                    isTiltGesturesEnabled = false
                    isZoomControlEnabled = false
                    isCompassEnabled = false
                    isScaleBarEnabled = false
                    isLogoClickEnabled = false
                }
                naverMap = map
            }
        )

        // 미리보기 지도는 확대/이동이 막혀있어서, 탭하면 자세히보기로 이동한다는 걸 알려주는 투명 오버레이
        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { onClick() }
        )

        val map = naverMap
        val pinMarkers = remember { mutableListOf<Marker>() }
        LaunchedEffect(map, pins) {
            if (map == null) return@LaunchedEffect
            pinMarkers.forEach { it.map = null }
            pinMarkers.clear()

            pins.forEach { pin ->
                val lat = pin.latitude ?: return@forEach
                val lng = pin.longitude ?: return@forEach
                val marker = Marker().apply {
                    position = LatLng(lat, lng)
                    applyPingIcon()
                    this.map = map
                }
                pinMarkers.add(marker)
            }

            val points = pins.mapNotNull { p -> p.latitude?.let { la -> p.longitude?.let { lo -> LatLng(la, lo) } } }
            cameraUpdateToShowAll(points, paddingPx = 80, fallbackZoom = 14.0)?.let { map.moveCamera(it) }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = visitedPlaceCount.toString(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
                Text(text = "다녀온 장소", fontSize = 11.sp, color = ColorTextSecondary)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MyPageScreenPreview() {
    MyPageScreen()
}

@Composable
private fun EmptyStateText(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 13.sp, color = ColorTextSecondary)
    }
}
