package com.tripping.app.ui.screen

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.PathOverlay
import com.tripping.app.ui.component.NaverMapContainer
import com.tripping.app.ui.model.HomeCourseCard

// ===== "내 주변 코스" 더보기 화면 (홈 > 내 주변 코스) - 피그마 node 88:3521 =====
// 피그마상 지도 영역은 정적 이미지(image 9, 363x431, cornerRadius16) mock이지만
// 실제 앱에서는 네이버 지도 SDK로 대체함(Client ID는 AndroidManifest.xml에 등록됨).
// TODO: 실제로는 사용자 현재 위치 기준 GET /courses/nearby 로 교체 (백엔드 아직 "시작 전")
@Composable
fun NearbyCoursesScreen(
    onBackClick: () -> Unit = {},
    onCourseClick: (Int) -> Unit = {}
) {
    val courses = remember {
        listOf(
            HomeCourseCard(1, null, "A 코스", listOf("강남", "코엑스", "석촌호수"), emptyList(), 5, 4.8, 31),
            HomeCourseCard(2, null, "A 코스", listOf("강남", "코엑스", "석촌호수"), emptyList(), 5, 4.8, 31),
            HomeCourseCard(3, null, "A 코스", listOf("강남", "코엑스", "석촌호수"), emptyList(), 5, 4.8, 31)
        )
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

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun NearbyCoursesScreenPreview() {
    NearbyCoursesScreen()
}
