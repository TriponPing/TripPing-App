package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.ui.model.HomeCourseCard
import com.tripping.app.ui.model.HomeKeyword
import com.tripping.app.viewmodel.HomeViewModel

// ===== "인기 키워드" 더보기 화면 (홈 > 이번주 인기 키워드) - 피그마 node 74:3178 =====
// 화면 상단 헤더(‹ + 타이틀)는 74:3178에 별도 텍스트 노드가 없어서, 동일 위치(25,30 / 66,34)를 쓰는
// 자매 화면 "내 주변 코스"(88:3521)의 헤더 패턴을 그대로 재사용함.
// 실제 API(GET /keyword/popular) 연동됨 - Ping 후기 등록 시 같이 남기는 해시태그 기준 집계.
// 키워드 칩을 누르면 그 해시태그가 달린 루트 목록(GET /keyword/{keyword}/routes)을 카드로 보여줌.
// 카드는 "내 주변 코스"와 동일한 HomeCourseCardView를 재사용함(피그마상 지도 썸네일 + 태그 칩 있는 카드).
// 코스 이름(courseName)은 DB에 여행 제목 필드가 없어서(팀 논의 결과) 빈 문자열로 둠 - 지어내지 않음.

// 키워드 칩은 한 줄에 최대 이만큼만 (그 이상이면 다음 줄로 안 넘기고 자름)
private const val MAX_KEYWORDS = 5

// 방문 장소 이름이 길면(특히 5개 이상) 카드 한 줄에 다 못 들어가서 레이아웃이 깨져서 상한을 둠
private const val MAX_STOPS_SHOWN = 3

@Composable
fun PopularKeywordsScreen(
    // 홈 화면에서 특정 키워드 칩을 눌러 들어온 경우, 그 키워드가 이미 선택된 채로 시작함
    initialKeyword: String? = null,
    onBackClick: () -> Unit = {},
    onCourseClick: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val popularKeywords by viewModel.popularKeywords.collectAsState()
    val selectedKeyword by viewModel.selectedKeyword.collectAsState()
    val keywordRoutes by viewModel.keywordRoutes.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadPopularKeywords(limit = MAX_KEYWORDS)
        if (initialKeyword != null) {
            viewModel.toggleKeywordSelection(initialKeyword)
        }
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
                if (popularKeywords.isNotEmpty()) {
                    KeywordChipsRow(
                        keywords = popularKeywords.map { it.keyword },
                        selectedKeyword = selectedKeyword,
                        onKeywordClick = { viewModel.toggleKeywordSelection(it) }
                    )
                } else {
                    Text(
                        text = "아직 이번 주 인기 키워드가 없어요",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = HomeGrayText
                    )
                }
            }

            if (selectedKeyword != null) {
                if (keywordRoutes.isEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "이 키워드가 달린 루트가 아직 없어요",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = HomeGrayText
                        )
                    }
                } else {
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                    items(keywordRoutes) { trip ->
                        val card = HomeCourseCard(
                            id = trip.routeId.toInt(),
                            authorName = trip.writerNickname,
                            courseName = "",
                            stops = trip.stopNames.take(MAX_STOPS_SHOWN),
                            tags = trip.tags.map { HomeKeyword(text = "#$it", selected = it == selectedKeyword) },
                            pingCount = trip.pingCount.toInt(),
                            distanceKm = totalDistanceKmOrNull(trip.coordinates) ?: 0.0,
                            bookmarkCount = trip.savedCount.toInt()
                        )
                        HomeCourseCardView(
                            course = card,
                            showArrows = true,
                            showAvatar = true,
                            onClick = { onCourseClick(trip.routeId) }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

// 최대 5개까지만, 줄바꿈 없이 한 줄로(넘치면 가로 스크롤)
@Composable
private fun KeywordChipsRow(
    keywords: List<String>,
    selectedKeyword: String?,
    onKeywordClick: (String) -> Unit
) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keywords.take(MAX_KEYWORDS).forEach { keyword ->
            HomeKeywordChip(
                keyword = HomeKeyword(text = "#$keyword", selected = keyword == selectedKeyword),
                onClick = { onKeywordClick(keyword) }
            )
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
