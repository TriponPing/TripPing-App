package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.tripping.app.R
import java.util.Locale
import com.tripping.app.data.response.PlaceSearchResponse

private val AccentBlue = Color(0xFF0074CE)
private val GrayText = Color(0xFF818181)
private val LightGrayBorder = Color(0xFFE0E0E0)

/**
 * 루트에 추가할 장소를 검색하는 화면.
 * 검색 결과를 탭하면 지도에 핑이 찍히고, 하단 정보 카드(이름/카테고리/가장 많이 가는 시간/핑 개수)가 뜨고,
 * + 버튼으로 루트에 추가할 수 있어요.
 *
 * 검색은 우리 DB로만 이뤄져요 (query 바뀔 때마다 상위에서 API 호출해서 results를 채워줘야 해요).
 */
@Composable
fun RoutePlaceSearchScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    results: List<PlaceSearchResponse>,
    onAddPlace: (RoutePlaceItem) -> Unit,
    onBackClick: () -> Unit = {}
) {
    var selectedPlace by remember { mutableStateOf<PlaceSearchResponse?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        // ===== 상단 뒤로가기 + 검색창 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "뒤로가기", tint = Color.Black)
            }
            OutlinedTextField(
                value = query,
                onValueChange = {
                    onQueryChange(it)
                    selectedPlace = null
                },
                placeholder = { Text("장소를 검색해보세요") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null, tint = GrayText) },
                singleLine = true,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier.weight(1f)
            )
        }
        HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)

        // ===== 지도 + (결과 리스트 or 선택된 장소 정보 카드) =====
        Box(modifier = Modifier.weight(1f)) {
            NaverSearchMapView(
                modifier = Modifier.fillMaxSize(),
                place = selectedPlace
            )

            if (selectedPlace == null) {
                if (query.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                    ) {
                        if (results.isEmpty()) {
                            Text(
                                text = "검색 결과가 없어요",
                                fontSize = 13.sp,
                                color = GrayText,
                                modifier = Modifier.padding(20.dp)
                            )
                        } else {
                            LazyColumn {
                                itemsIndexed(results) { _, place ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedPlace = place }
                                            .padding(horizontal = 20.dp, vertical = 14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = place.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = place.category, fontSize = 13.sp, color = GrayText)
                                        }
                                    }
                                    HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)
                                }
                            }
                        }
                    }
                }
            } else {
                val place = selectedPlace!!
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .shadow(6.dp, RoundedCornerShape(16.dp))
                        .background(Color.White, RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    // 이름 + 카테고리 태그 + 추가 버튼
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = place.name, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE3F2FD), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = place.category, fontSize = 12.sp, color = AccentBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(AccentBlue, CircleShape)
                                .clickable {
                                    onAddPlace(
                                        RoutePlaceItem(
                                            order = 0,
                                            name = place.name,
                                            tags = listOf(place.category),
                                            latitude = place.latitude,
                                            longitude = place.longitude
                                        )
                                    )
                                    onBackClick()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "루트에 추가", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // 가장 많이 가는 시간 / 핑 개수
                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoBlock(
                            icon = Icons.Filled.AccessTime,
                            label = "가장 많이 가는 시간",
                            value = place.popularTimeSlot ?: "정보 없음",
                            modifier = Modifier.weight(1f)
                        )
                        InfoBlock(
                            icon = Icons.Filled.LocationOn,
                            label = "핑 개수",
                            value = "${place.pingCount}",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoBlock(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.Top) {
        Icon(icon, contentDescription = null, tint = GrayText, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = GrayText)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
    }
}

@Composable
private fun NaverSearchMapView(
    modifier: Modifier = Modifier,
    place: PlaceSearchResponse?
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current
    val marker = remember { mutableStateOf<Marker?>(null) }

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

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.getMapAsync { naverMap ->
                // 👈 수정: locale 지정 안 하면 기기 시스템 언어를 따라가서 지도 위 장소 이름(POI)이
                // 영어로 나올 수 있음 - 한국어로 고정.
                naverMap.locale = Locale.KOREA
                marker.value?.map = null
                marker.value = null

                if (place != null) {
                    val latLng = LatLng(place.latitude, place.longitude)
                    val newMarker = Marker().apply {
                        position = latLng
                        icon = OverlayImage.fromResource(R.drawable.map_ping)
                        map = naverMap
                    }
                    marker.value = newMarker
                    naverMap.moveCamera(CameraUpdate.scrollTo(latLng))
                }
            }
        }
    )
}
