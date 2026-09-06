package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.R
import com.tripping.app.data.response.PopularPlaceResponse
import com.tripping.app.viewmodel.HomeViewModel
import com.tripping.app.viewmodel.PlaceCategory

// ===== "떠오르는 인기 장소" 더보기 화면 (홈 > 떠오르는 인기 장소) - 피그마 node 457:736 =====
// 저장(찜) 수가 많은 순 TOP 30. 동점(저장 수 같음)은 같은 등수로 표시하고(dense rank),
// 저장 수가 같아서 30개보다 더 뽑히는 경우는 없음 - 백엔드에서 이미 상위 30건만 내려줌.
// 피그마 목업의 "지도 핀 + 2" 표기는 실데이터 근거가 없는 목업 placeholder라 화면에서 제외함(사용자 확인 완료).
// 한 페이지에 10개씩, 총 3페이지 - 30개를 한 번에 받아와서 화면에서만 나눠 보여줌(서버 페이지네이션 불필요).
private const val PLACES_PER_PAGE = 10

@Composable
fun PopularPlacesScreen(
    onBackClick: () -> Unit = {},
    onPlaceClick: (Long) -> Unit = {},
    viewModel: HomeViewModel = viewModel()
) {
    val popularPlaces by viewModel.popularPlaces.collectAsState()
    LaunchedEffect(Unit) {
        viewModel.loadPopularPlaces(limit = 30)
    }

    var page by remember { mutableStateOf(0) }
    val pageCount = if (popularPlaces.isEmpty()) 1 else (popularPlaces.size + PLACES_PER_PAGE - 1) / PLACES_PER_PAGE
    val ranked = remember(popularPlaces) { computeDenseRanks(popularPlaces) }
    val pageItems = ranked.drop(page * PLACES_PER_PAGE).take(PLACES_PER_PAGE)

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        PageHeader(title = "떠오르는 인기 장소", onBackClick = onBackClick)

        LazyColumn(
            modifier = Modifier.fillMaxSize().background(HomePageBg),
            contentPadding = PaddingValues(horizontal = 21.dp, vertical = 20.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = buildAnnotatedString {
                                append("지금, 여행자들이 ")
                                withStyle(SpanStyle(color = HomeAccentBlue)) { append("가장 많이 가는 곳") }
                                append("이에요!")
                            },
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E2E2E)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "다른 여행자들이 저장한 여행지를 만나보세요.",
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
                Spacer(modifier = Modifier.height(20.dp))
                Text(text = "이번 주 인기 장소 TOP 30", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E2E2E))
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (popularPlaces.isEmpty()) {
                item {
                    Text(
                        text = "아직 이번 주 인기 장소가 없어요",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = HomeGrayText
                    )
                }
            }

            items(pageItems) { (rank, place) ->
                PopularPlaceCard(rank = rank, place = place, onClick = { onPlaceClick(place.spotId) })
                Spacer(modifier = Modifier.height(16.dp))
            }

            if (popularPlaces.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    PageControl(
                        page = page,
                        pageCount = pageCount,
                        onPrev = { if (page > 0) page-- },
                        onNext = { if (page < pageCount - 1) page++ }
                    )
                }
            }
        }
    }
}

// 저장 수가 같으면 같은 등수를 매기고, 다음으로 다른 값이 나오면 그 직전까지의 개수만큼 건너뛰지 않고
// 순번을 1씩만 올리는 "dense rank" 방식 (예: 5위 두 명이면 다음은 6위, 7위가 아니라 6위).
private fun computeDenseRanks(places: List<PopularPlaceResponse>): List<Pair<Int, PopularPlaceResponse>> {
    var rank = 0
    var lastCount: Long? = null
    return places.map { place ->
        if (lastCount == null || place.savedCount != lastCount) {
            rank += 1
            lastCount = place.savedCount
        }
        rank to place
    }
}

@Composable
private fun PopularPlaceCard(rank: Int, place: PopularPlaceResponse, onClick: () -> Unit) {
    val shadowColor = if (rank == 1) Color(0xFFFFBF00) else Color.Black.copy(alpha = 0.25f)

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp), ambientColor = shadowColor, spotColor = shadowColor)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Box(modifier = Modifier.width(40.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (rank) {
                    1 -> Image(painter = painterResource(R.drawable.home_crown_gold), contentDescription = null, modifier = Modifier.size(24.dp))
                    2 -> Image(painter = painterResource(R.drawable.home_crown_silver), contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text(text = rank.toString(), fontSize = 24.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = place.name, fontSize = 22.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            if (!place.category.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = PlaceCategory.labelOf(place.category), fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = HomeGrayText)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Outlined.BookmarkBorder, contentDescription = null, tint = HomeGrayText, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = place.savedCount.toString(), fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HomeTextPrimary)
        }
    }
}

@Composable
private fun PageControl(page: Int, pageCount: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "<",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (page > 0) HomeTextPrimary else HomeGrayText,
            modifier = Modifier.clickable(enabled = page > 0) { onPrev() }.padding(8.dp)
        )
        Text(text = "${page + 1} / $pageCount", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = HomeTextPrimary)
        Text(
            text = ">",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (page < pageCount - 1) HomeTextPrimary else HomeGrayText,
            modifier = Modifier.clickable(enabled = page < pageCount - 1) { onNext() }.padding(8.dp)
        )
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PopularPlacesScreenPreview() {
    PopularPlacesScreen()
}
