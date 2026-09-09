package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.ui.model.HomeKeyword
import com.tripping.app.viewmodel.HomeViewModel

// ===== "인기 키워드" 더보기 화면 (홈 > 이번주 인기 키워드) - 피그마 node 74:3178 =====
// 화면 상단 헤더(‹ + 타이틀)는 74:3178에 별도 텍스트 노드가 없어서, 동일 위치(25,30 / 66,34)를 쓰는
// 자매 화면 "내 주변 코스"(88:3521)의 헤더 패턴을 그대로 재사용함.
// 실제 API(GET /keyword/popular) 연동됨 - Ping 후기 등록 시 같이 남기는 해시태그 기준 집계.
// 피그마 목업엔 이 키워드로 필터링된 코스 목록도 있었지만, "키워드로 코스 검색" API가 아직 없어서
// (하드코딩/mock 금지 원칙상) 뺐음 - 생기면 추가.
@Composable
fun PopularKeywordsScreen(
    onBackClick: () -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val popularKeywords by viewModel.popularKeywords.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadPopularKeywords(limit = 30) }

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
                    FlowKeywordChips(keywords = popularKeywords.map { "#${it.keyword}" })
                } else {
                    Text(
                        text = "아직 이번 주 인기 키워드가 없어요",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = HomeGrayText
                    )
                }
            }
        }
    }
}

// 칩 개수가 가변적이라 한 줄 Row 대신, 화면 너비를 넘기면 다음 줄로 넘어가도록 직접 줄바꿈 처리
@Composable
private fun FlowKeywordChips(keywords: List<String>) {
    val rows = remember(keywords) { keywords.chunked(3) }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { text -> HomeKeywordChip(keyword = HomeKeyword(text = text)) }
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
