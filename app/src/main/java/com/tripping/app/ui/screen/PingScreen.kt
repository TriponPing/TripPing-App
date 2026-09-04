package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripping.app.R

// ===== 색상 =====
private val BluePrimary = Color(0xFF4A72C4)
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)
private val CardBorder = Color(0xFFECECEC)

// ===== "기록" 탭에서 쓸 모델 =====
data class PingItem(
    val id: Int,
    val placeName: String,
    val time: String,
    val status: PingStatus
)

enum class PingStatus { DONE, CURRENT, UPCOMING }

// ===== "로그" 탭에서 쓸 모델 (지역별 코스 카드) =====
data class PingLogCourse(
    val id: Int,
    val authorName: String,
    val courseName: String,
    val stops: List<String>,   // 예: ["강남", "코엑스", "석촌호수"]
    val pingCount: Int,
    val bookmarkCount: Int,
    val distanceKm: Double
)

private enum class PingTab { RECORD, LOG }

@Composable
fun PingScreen(
    onAddPingClick: () -> Unit = {},
    onPingLogClick: (Int) -> Unit = {},
    onRouteCardClick: (Int) -> Unit = {},
    onCourseClick: (Int) -> Unit = {}
) {
    var selectedTab by remember { mutableStateOf(PingTab.RECORD) }

    Scaffold(
        topBar = {
            Text(
                text = if (selectedTab == PingTab.RECORD) "Ping 기록" else "Ping 로그 커뮤니티",
                fontSize = 13.sp,
                color = GrayText,
                modifier = Modifier.padding(start = 20.dp, top = 12.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
                Text(text = "Ping", fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))
                TabToggle(selectedTab = selectedTab, onTabSelected = { selectedTab = it })
            }

            when (selectedTab) {
                PingTab.RECORD -> PingRecordContent(
                    onAddPingClick = onAddPingClick,
                    onPingLogClick = onPingLogClick,
                    onRouteCardClick = onRouteCardClick
                )
                PingTab.LOG -> PingLogContent(
                    onCourseClick = onCourseClick
                )
            }
        }
    }
}

// ===================================================================
// "기록" 탭 콘텐츠
// ===================================================================
@Composable
private fun PingRecordContent(
    onAddPingClick: () -> Unit,
    onPingLogClick: (Int) -> Unit,
    onRouteCardClick: (Int) -> Unit
) {
    // TODO: ViewModel에서 PingApi.getOngoingPings(routeId) 호출 결과로 교체
    val mockPings = remember {
        listOf(
            PingItem(1, "ABC카페", "12시 30분", PingStatus.DONE),
            PingItem(2, "ABC카페", "12시 30분", PingStatus.CURRENT),
            PingItem(3, "ABC카페", "12시 30분", PingStatus.UPCOMING)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Text(text = "여행을 기록해보아요!", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            TimelineDots(pings = mockPings)
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(mockPings) { ping ->
            PingCard(ping = ping, onLinkClick = { onPingLogClick(ping.id) })
            Spacer(modifier = Modifier.height(10.dp))
        }

        item {
            AddPingButton(onClick = onAddPingClick)
            Spacer(modifier = Modifier.height(12.dp))

            HintBanner()
            Spacer(modifier = Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "기록을 추가하고 싶은 핑로그가 있나요?", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "바로가기", fontSize = 13.sp, color = GrayText)
            }
            Spacer(modifier = Modifier.height(12.dp))

            // TODO: 최근 루트 목록 API 연동 시 리스트로 교체
            RouteCard(
                title = "쌍문 코스",
                dateText = "2026년 8월 13일 마지막 수정",
                pingCount = 4,
                onClick = { onRouteCardClick(1) }
            )
        }
    }
}

@Composable
private fun TabToggle(selectedTab: PingTab, onTabSelected: (PingTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .background(GrayBg, RoundedCornerShape(22.dp))
            .padding(4.dp)
    ) {
        TabItem(
            text = "기록",
            selected = selectedTab == PingTab.RECORD,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(PingTab.RECORD) }
        )
        TabItem(
            text = "로그",
            selected = selectedTab == PingTab.LOG,
            modifier = Modifier.weight(1f),
            onClick = { onTabSelected(PingTab.LOG) }
        )
    }
}

@Composable
private fun TabItem(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) BluePrimary else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (selected) Color.White else GrayText, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

private fun pingDrawable(status: PingStatus): Int = when (status) {
    PingStatus.DONE -> R.drawable.blueping
    PingStatus.CURRENT -> R.drawable.greenping
    PingStatus.UPCOMING -> R.drawable.blueping
}

@Composable
private fun TimelineDots(pings: List<PingItem>) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        pings.forEachIndexed { index, ping ->
            Image(painter = painterResource(id = pingDrawable(ping.status)), contentDescription = null, modifier = Modifier.size(16.dp))
            if (index != pings.lastIndex) {
                Box(modifier = Modifier.weight(1f).height(2.dp).background(BluePrimary))
            }
        }
    }
}

@Composable
private fun PingCard(ping: PingItem, onLinkClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(painter = painterResource(id = pingDrawable(ping.status)), contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = ping.placeName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = ping.time, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "장소와 시간이 맞나요?", fontSize = 12.sp, color = GrayText, modifier = Modifier.weight(1f))
                Text(
                    text = "핑로그 기록/수정 하러가기",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black,
                    modifier = Modifier.clickable { onLinkClick() }
                )
                Text(text = "›", fontSize = 14.sp, color = GrayText, modifier = Modifier.padding(start = 2.dp))
            }
        }
    }
}

@Composable
private fun AddPingButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(painter = painterResource(id = R.drawable.plus_circle), contentDescription = "핑 추가", modifier = Modifier.size(22.dp))
    }
}

@Composable
private fun HintBanner() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GrayBg, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "핑을 꾹 눌러 순서를 변경할 수 있어요   잊은 핑을 추가해보세요", fontSize = 11.sp, color = GrayText, modifier = Modifier.weight(1f))
        Text(text = "×", fontSize = 13.sp, color = GrayText)
    }
}

@Composable
private fun RouteCard(title: String, dateText: String, pingCount: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)).background(GrayBg))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = dateText, fontSize = 11.sp, color = GrayText)
        }
        Text(text = "핑 ${pingCount}개", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = BluePrimary)
    }
}

// ===================================================================
// "로그" 탭 콘텐츠 - 지역별 코스 커뮤니티 리스트
// ===================================================================
@Composable
private fun PingLogContent(onCourseClick: (Int) -> Unit) {
    var selectedRegion by remember { mutableStateOf("지역") }

    // TODO: 실제로는 지역 선택에 따른 API 호출 결과로 교체
    val mockCourses = remember {
        listOf(
            PingLogCourse(1, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"), 5, 31, 4.8),
            PingLogCourse(2, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"), 5, 31, 4.8),
            PingLogCourse(3, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"), 5, 31, 4.8),
            PingLogCourse(4, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"), 5, 31, 4.8)
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RegionDropdown(
                    selectedRegion = selectedRegion,
                    onRegionSelected = { selectedRegion = it }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "지역별 인기순위를 만나보세요", fontSize = 12.sp, color = GrayText)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(mockCourses) { course ->
            PingLogCourseCard(course = course, onClick = { onCourseClick(course.id) })
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun RegionDropdown(selectedRegion: String, onRegionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val regions = listOf("전체", "강남", "홍대", "성수", "여의도")

    Box {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedRegion, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "⌄", fontSize = 18.sp, color = Color.Black)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            regions.forEach { region ->
                DropdownMenuItem(
                    text = { Text(region) },
                    onClick = {
                        onRegionSelected(region)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun PingLogCourseCard(course: PingLogCourse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(GrayBg)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = course.authorName, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.Top) {
            // TODO: 실제 경로 미리보기 이미지/지도로 교체
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8EEF5))
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = course.courseName, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                    Text(text = "🔖", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = course.bookmarkCount.toString(), fontSize = 12.sp, color = GrayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = course.stops.joinToString(" → "),
                    fontSize = 13.sp,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Ping 개수 : ${course.pingCount}개", fontSize = 11.sp, color = GrayText)
                    Text(text = "${course.distanceKm} km", fontSize = 11.sp, color = GrayText)
                }
            }
        }
    }
}