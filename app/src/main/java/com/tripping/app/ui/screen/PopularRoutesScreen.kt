package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import android.graphics.Color as AndroidColor
import coil.compose.AsyncImage
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.PathOverlay
import com.tripping.app.R
import com.tripping.app.data.response.PopularTripResponse
import com.tripping.app.data.response.RouteCoordinate
import com.tripping.app.ui.component.NaverMapContainer
import com.tripping.app.ui.component.cameraUpdateToShowAll
import com.tripping.app.viewmodel.HomeViewModel
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

// ===== "이번 주 인기 루트" 더보기 화면 (홈 > 이번 주 인기 루트) - 피그마 node 422:737 =====
// "여행 이름"은 DB에 여행 제목 필드가 없어서(팀 논의 결과) 뺐음 - 지어내지 않음.
// 지도 미리보기는 실제 GPS 핑 좌표(coordinates)가 2개 이상 있는 루트만 그리고,
// 부족하면 대표 사진으로 대체함(피그마 목업의 손그림 경로선은 실제 지도로 대체).
@Composable
fun PopularRoutesScreen(
    onBackClick: () -> Unit = {},
    onCourseClick: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val popularTrips by viewModel.popularTrips.collectAsState()
    val savedRouteIds by viewModel.savedRouteIds.collectAsState()
    val savedCountDeltas by viewModel.savedCountDeltas.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadPopularTrips(limit = 20)
        viewModel.loadSavedRouteIds()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        PageHeader(title = "이번주 인기 루트", onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize().background(HomePageBg),
            contentPadding = PaddingValues(horizontal = 21.dp, vertical = 20.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    // 아이콘이 텍스트보다 훨씬 커서 Row에 같이 넣으면 아이콘 높이만큼 아래 텍스트가 밀려버림 ->
                    // Box로 겹쳐서 텍스트 흐름(제목+설명)은 서로 붙어있게 하고 아이콘만 우상단에 얹음
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "사람들이 많이 저장한 \n인기 루트를 확인해보세요",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E2E2E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "다른 여행자들이 저장한 여행 코스를 만나보세요.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = HomeGrayText
                        )
                    }
                    Image(
                        painter = painterResource(R.drawable.home_popular_route_icon),
                        contentDescription = null,
                        modifier = Modifier.size(100.dp).align(Alignment.TopEnd)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(popularTrips) { trip ->
                PopularRouteListCard(
                    trip = trip,
                    isSaved = trip.routeId in savedRouteIds,
                    countDelta = savedCountDeltas[trip.routeId] ?: 0,
                    onClick = { onCourseClick(trip.routeId) },
                    onBookmarkClick = { viewModel.toggleSaveRoute(trip.routeId) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun PopularRouteListCard(
    trip: PopularTripResponse,
    isSaved: Boolean,
    countDelta: Int,
    onClick: () -> Unit,
    onBookmarkClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(8.dp), ambientColor = Color.Black.copy(alpha = 0.25f))
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(HomeAvatarGray))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = trip.writerNickname ?: "알 수 없음",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = HomeTextPrimary,
                modifier = Modifier.weight(1f)
            )
            BookmarkIcon(filled = isSaved, modifier = Modifier.clickable { onBookmarkClick() })
            Spacer(modifier = Modifier.width(4.dp))
            // 서버가 준 총 저장 수는 화면 진입 시점 스냅샷이라, 이 화면에서 내가 저장/취소한
            // 만큼만(+1/-1) 보정해서 바로 반영함(낙관적 업데이트)
            val displayedCount = trip.savedCount + countDelta
            Text(text = displayedCount.toString(), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HomeTextPrimary)
        }
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.Top) {
            RouteThumbnail(trip = trip, modifier = Modifier.size(83.dp).clip(RoundedCornerShape(8.dp)))
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (trip.stopNames.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        trip.stopNames.forEachIndexed { index, stop ->
                            Text(text = stop, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HomeTextPrimary)
                            if (index != trip.stopNames.lastIndex) {
                                Text(text = "  →  ", fontSize = 13.sp, color = HomeTextPrimary)
                            }
                        }
                    }
                } else {
                    Text(text = "등록된 Ping 기록이 없어요", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = HomeGrayText)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(text = "Ping 개수 : ${trip.pingCount}개", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HomeGrayText)
                    val distanceKm = totalDistanceKmOrNull(trip.coordinates)
                    if (distanceKm != null) {
                        Text(text = "%.1f km".format(distanceKm), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = HomeGrayText)
                    }
                }
            }
        }
    }
}

// 좌표가 2개 이상 있으면 실제 지도 위에 방문 순서대로 이은 경로선을 그리고,
// 부족하면(0~1개) 대표 사진으로 대체함 (지도로 그릴 경로 자체가 성립하지 않으므로).
@Composable
private fun RouteThumbnail(trip: PopularTripResponse, modifier: Modifier = Modifier) {
    if (trip.coordinates.size >= 2) {
        var naverMap by remember { mutableStateOf<NaverMap?>(null) }
        val points = remember(trip.coordinates) { trip.coordinates.map { LatLng(it.latitude, it.longitude) } }

        LaunchedEffect(naverMap, points) {
            val map = naverMap ?: return@LaunchedEffect
            map.uiSettings.apply {
                isZoomControlEnabled = false
                isCompassEnabled = false
                isScaleBarEnabled = false
                isLocationButtonEnabled = false
                setAllGesturesEnabled(false)
            }
            PathOverlay().apply {
                coords = points
                color = AndroidColor.parseColor("#FF0000")
                width = 4
                this.map = map
            }
            cameraUpdateToShowAll(points, paddingPx = 8)?.let { map.moveCamera(it) }
        }

        NaverMapContainer(modifier = modifier, onMapReady = { naverMap = it })
    } else {
        val photoModifier = modifier
        if (!trip.photoUrl.isNullOrBlank()) {
            AsyncImage(model = trip.photoUrl, contentDescription = null, contentScale = ContentScale.Crop, modifier = photoModifier)
        } else {
            Image(
                painter = painterResource(R.drawable.home_popular_route_photo),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = photoModifier
            )
        }
    }
}

// 방문 순서대로 이웃한 좌표 사이 거리(하버사인)를 다 더한 총 이동거리. 좌표가 2개 미만이면 계산 불가하므로 null.
private fun totalDistanceKmOrNull(coordinates: List<RouteCoordinate>): Double? {
    if (coordinates.size < 2) return null
    var total = 0.0
    for (i in 0 until coordinates.lastIndex) {
        total += haversineKm(coordinates[i], coordinates[i + 1])
    }
    return total
}

private fun haversineKm(a: RouteCoordinate, b: RouteCoordinate): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(b.latitude - a.latitude)
    val dLon = Math.toRadians(b.longitude - a.longitude)
    val sinA = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(a.latitude)) * cos(Math.toRadians(b.latitude)) * sin(dLon / 2) * sin(dLon / 2)
    val c = 2 * atan2(sqrt(sinA), sqrt(1 - sinA))
    return earthRadiusKm * c
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PopularRoutesScreenPreview() {
    PopularRoutesScreen()
}
