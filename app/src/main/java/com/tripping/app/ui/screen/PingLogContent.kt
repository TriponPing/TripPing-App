package com.tripping.app.ui.screen

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripping.app.viewmodel.PingLogCourse

// ===== 색상 (필요시 공통 파일로 관리) =====
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)
private val CardBorder = Color(0xFFECECEC)

@Composable
internal fun PingLogContent(onCourseClick: (Long) -> Unit) { // 👈 course.id가 Long이므로 Int에서 Long으로 변경
    var selectedRegion by remember { mutableStateOf("지역") }

    // TODO: 실제로는 지역 선택에 따른 API 호출 결과로 교체
    val mockCourses = remember {
        listOf(
            PingLogCourse(1L, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"), 5, 31, 4),
            PingLogCourse(2L, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"), 5, 31, 4),
            PingLogCourse(3L, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"), 5, 31, 4),
            PingLogCourse(4L, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"), 5, 31, 4)
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