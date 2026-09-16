package com.tripping.app.ui.screen

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
import com.tripping.app.ui.model.HomeCourseCard
import com.tripping.app.ui.model.HomeKeyword
import com.tripping.app.viewmodel.HomeViewModel

// ===== "내 주변 코스" 더보기 화면 (홈 > 내 주변 코스) - 피그마 node 88:3521 =====
// 피그마상 지도 영역은 정적 이미지(image 9, 363x431, cornerRadius16) mock이지만
// 실제 앱에서는 네이버 지도 SDK로 대체함(Client ID는 AndroidManifest.xml에 등록됨).
// 👈 수정: 목데이터 목록 -> 홈이랑 같은 방식(마지막으로 찍은 핑 좌표 기준 GET /trips/nearby)으로 교체.
// 지도 위 마커/경로는 여전히 mock(TODO 아래) - 이번 요청 범위는 "목록 전체 보이게"라서 목록만 실데이터로 바꿈.
@Composable
fun NearbyCoursesScreen(
    onBackClick: () -> Unit = {},
    onCourseClick: (Int) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val currentTrip by viewModel.currentTrip.collectAsState()
    val nearbyTripsRaw by viewModel.nearbyCourses.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadCurrentTrip() // 성공하면 마지막 핑 좌표가 있을 때 내부적으로 loadNearbyCourses까지 이어서 호출됨
    }

    val courses = remember(nearbyTripsRaw) {
        nearbyTripsRaw.map { trip ->
            HomeCourseCard(
                id = trip.routeId.toInt(),
                authorName = null,
                courseName = trip.title ?: "여행 루트",
                stops = trip.stopNames,
                tags = trip.tags.map { HomeKeyword(text = "#$it") },
                pingCount = trip.pingCount.toInt(),
                distanceKm = trip.distanceKm ?: 0.0,
                bookmarkCount = trip.savedCount.toInt()
            )
        }
    }

    var naverMap by remember { mutableStateOf<NaverMap?>(null) }

    // TODO: 목데이터 좌표 - 실제로는 API가 내려주는 코스 대표 좌표로 교체
    LaunchedEffect(naverMap) {
        val map = naverMap ?: return@LaunchedEffect
        val center = LatLng(37.4563, 126.8951)
        val routePoints = listOf(
            LatLng(37.4520, 126.8900),
            LatLng(37.4545, 126.8930),
            center
        )

        Marker().apply {
            position = center
            captionText = "서울 금천구 우체국"
            iconTintColor = AndroidColor.parseColor("#E23E57")
            this.map = map
        }
        PathOverlay().apply {
            coords = routePoints
            color = AndroidColor.parseColor("#C23FA0")
            width = 8
            this.map = map
        }
        map.moveCamera(CameraUpdate.scrollTo(center))
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        PageHeader(title = "내 주변 코스", onBackClick = onBackClick)

        NaverMapContainer(
            modifier = Modifier
                .padding(horizontal = 21.dp)
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp)),
            onMapReady = { naverMap = it }
        )

        if (courses.isEmpty()) {
            Text(
                text = if (currentTrip?.lastPingLatitude != null) "마지막 핑 주변에 아직 등록된 코스가 없어요"
                       else "Ping을 찍으면 그 주변 코스를 보여드려요",
                fontSize = 13.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                color = Color(0xFF9A9A9A),
                modifier = Modifier.padding(horizontal = 21.dp, vertical = 20.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 21.dp, vertical = 20.dp)
            ) {
                items(courses) { course ->
                    HomeCourseCardView(
                        course = course,
                        showArrows = true,
                        titleFontSize = 13.sp,
                        onClick = { onCourseClick(course.id) }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NearbyCoursesScreenPreview() {
    NearbyCoursesScreen()
}
