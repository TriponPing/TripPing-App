package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripping.app.ui.model.HomeCourseCard
import com.tripping.app.ui.model.HomeKeyword

// ===== "인기 키워드" 더보기 화면 (홈 > 이번주 인기 키워드) - 피그마 node 74:3178 =====
// 화면 상단 헤더(‹ + 타이틀)는 74:3178에 별도 텍스트 노드가 없어서, 동일 위치(25,30 / 66,34)를 쓰는
// 자매 화면 "내 주변 코스"(88:3521)의 헤더 패턴을 그대로 재사용함.
// TODO: 실제로는 GET /keywords/popular, GET /courses?keyword= 등으로 교체 (백엔드 아직 "시작 전")
@Composable
fun PopularKeywordsScreen(
    onBackClick: () -> Unit = {},
    onCourseClick: (Int) -> Unit = {}
) {
    val keywords = remember {
        listOf(
            HomeKeyword("# 바다", selected = true),
            HomeKeyword("# 여름 휴가"),
            HomeKeyword("# 야경"),
            HomeKeyword("#데이트")
        )
    }
    val courses = remember {
        listOf(
            HomeCourseCard(
                1, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"),
                listOf(HomeKeyword("#데이트"), HomeKeyword("# 바다", selected = true)), 5, 4.8, 31
            ),
            HomeCourseCard(
                2, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"),
                listOf(HomeKeyword("#데이트"), HomeKeyword("# 바다", selected = true)), 5, 4.8, 31
            ),
            HomeCourseCard(
                3, "등록자이름", "A 코스", listOf("강남", "코엑스", "석촌호수"),
                listOf(HomeKeyword("#데이트"), HomeKeyword("# 바다", selected = true)), 5, 4.8, 31
            )
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        PageHeader(title = "인기 키워드", onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(HomePageBg),
            contentPadding = PaddingValues(horizontal = 21.dp, vertical = 20.dp)
        ) {
            item {
                Text(text = "이번 주 인기 키워드", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HomeTextPrimary)
                Spacer(modifier = Modifier.height(9.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    keywords.forEach { keyword -> HomeKeywordChip(keyword = keyword) }
                }
                Spacer(modifier = Modifier.height(30.dp))
            }

            items(courses) { course ->
                HomeCourseCardView(
                    course = course,
                    showArrows = true,
                    titleFontSize = 15.sp,
                    showAvatar = true,
                    onClick = { onCourseClick(course.id) }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// 인기 키워드/내 주변 코스 화면이 공유하는 상단 헤더 (‹ 뒤로가기 + 타이틀), 피그마 rel(25,30)/(66,34) 위치 기준
@Composable
internal fun PageHeader(title: String, onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 21.dp, end = 20.dp, top = 30.dp, bottom = 31.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "‹",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = HomeTextPrimary,
            modifier = Modifier.clickable { onBackClick() }.padding(end = 15.dp)
        )
        Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HomeTextPrimary)
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PopularKeywordsScreenPreview() {
    PopularKeywordsScreen()
}
