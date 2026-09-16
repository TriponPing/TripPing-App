// [파일 설명] Ping 탭 > 로그 커뮤니티 > "로그 상세보기" 화면 UI.
// 그 루트를 등록한 사람 정보 + 경유지 번호 경로 + (가로로 넘겨서 고르는) 장소별 후기를 보여줌.
package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tripping.app.data.response.PlaceDetailResponse
import com.tripping.app.data.response.PlaceReviewResponse
import com.tripping.app.data.response.PopularTripDetailResponse
import com.tripping.app.data.response.StopSummaryResponse
import com.tripping.app.viewmodel.PingLogDetailViewModel

private val AccentBlue = Color(0xFF0074CE)
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)
private val LightGrayBorder = Color(0xFFE0E0E0)
private val StarColor = Color(0xFFFFC107)

// 화면 안에서 바로 보여주는 후기 개수 (그 이상은 "후기 N개 모두 보기"로 펼침)
private const val COLLAPSED_REVIEW_COUNT = 3

@Composable
fun PingLogDetailScreen(
    routeId: Long,
    onBackClick: () -> Unit,
    viewModel: PingLogDetailViewModel = viewModel()
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val placeDetails by viewModel.placeDetails.collectAsState()
    val loadingPlaceIds by viewModel.loadingPlaceIds.collectAsState()

    LaunchedEffect(routeId) { viewModel.loadDetail(routeId) }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        when {
            isLoading && detail == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
            errorMessage != null && detail == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(text = errorMessage ?: "오류가 발생했어요", color = GrayText, fontSize = 14.sp)
                }
            }
            detail != null -> {
                PingLogDetailContent(
                    detail = detail!!,
                    isSaved = isSaved,
                    placeDetails = placeDetails,
                    loadingPlaceIds = loadingPlaceIds,
                    onBackClick = onBackClick,
                    onSaveClick = { viewModel.toggleSaveRoute(routeId) },
                    onPlaceSelected = { spotId -> viewModel.loadPlaceDetail(spotId) }
                )
            }
        }
    }
}

@Composable
private fun PingLogDetailContent(
    detail: PopularTripDetailResponse,
    isSaved: Boolean,
    placeDetails: Map<Long, PlaceDetailResponse>,
    loadingPlaceIds: Set<Long>,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onPlaceSelected: (Long) -> Unit
) {
    val stops = detail.stops

    Column(modifier = Modifier.fillMaxSize()) {
        // ===== 상단바: 뒤로가기 + 저장(북마크) + 더보기 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowLeft,
                contentDescription = "뒤로가기",
                tint = Color.Black,
                modifier = Modifier.size(26.dp).clickable { onBackClick() }
            )
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = "루트 저장",
                tint = AccentBlue,
                modifier = Modifier.size(24.dp).clickable { onSaveClick() }
            )
            Spacer(modifier = Modifier.width(14.dp))
            // 더보기(⋮) - 신고/공유 등은 아직 백엔드에 없어서 지금은 자리만 표시(동작 없음)
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = "더보기",
                tint = Color.Black,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ===== 작성자 프로필 카드 =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(56.dp).background(Color(0xFFEEEEEE), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val writerProfileImage = detail.writerProfileImage
                    if (!writerProfileImage.isNullOrBlank()) {
                        // 👈 수정: 프로필 사진은 지금 base64 데이터(data:image/...)로 저장되는데
                        // Coil의 AsyncImage에 그대로 넘기면 URL로 착각해서 요청하다가 무조건 실패함
                        // (그래서 안 불러와졌던 것). 마이페이지/설정 화면과 동일하게 직접 디코딩해서 그림.
                        // 혹시 나중에 진짜 URL 형태로 바뀌어도 디코딩 실패 시 AsyncImage로 대체되게 함.
                        val decodedBitmap = remember(writerProfileImage) { decodeProfileImage(writerProfileImage) }
                        if (decodedBitmap != null) {
                            androidx.compose.foundation.Image(
                                bitmap = decodedBitmap,
                                contentDescription = "프로필 사진",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            AsyncImage(
                                model = writerProfileImage,
                                contentDescription = "프로필 사진",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.title ?: "루트",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "@${detail.writerNickname}", fontSize = 13.sp, color = GrayText)
                    if (!detail.writerLevel.isNullOrBlank()) {
                        Text(text = detail.writerLevel, fontSize = 13.sp, color = GrayText)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = LightGrayBorder, thickness = 6.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // ===== 번호가 매겨진 경유지 경로 =====
            if (stops.isNotEmpty()) {
                NumberedPathRow(stops = stops)
                Spacer(modifier = Modifier.height(24.dp))
            }

            HorizontalDivider(color = LightGrayBorder, thickness = 6.dp)
            Spacer(modifier = Modifier.height(20.dp))

            // ===== 장소 후기 섹션 =====
            Text(
                text = "💬 이 루트의 장소 후기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(14.dp))

            if (stops.isEmpty()) {
                Text(
                    text = "등록된 장소가 없어요",
                    fontSize = 13.sp,
                    color = GrayText,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
                )
            } else {
                PlaceReviewPager(
                    stops = stops,
                    placeDetails = placeDetails,
                    loadingPlaceIds = loadingPlaceIds,
                    onPlaceSelected = onPlaceSelected
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// ===== 번호 원 + 연결선 + 장소 이름으로 이루어진 경로 =====
@Composable
private fun NumberedPathRow(stops: List<StopSummaryResponse>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.Top
    ) {
        stops.forEachIndexed { index, stop ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(68.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(AccentBlue),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (index + 1).toString(),
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = stop.name,
                    fontSize = 12.sp,
                    color = Color(0xFF333333),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (index < stops.size - 1) {
                Box(
                    modifier = Modifier
                        .padding(top = 13.dp)
                        .width(24.dp)
                        .height(2.dp)
                        .background(LightGrayBorder)
                )
            }
        }
    }
}

// ===== 가로로 넘기면서 장소를 고르는 셀렉터 + 선택된 장소의 후기 =====
@Composable
private fun PlaceReviewPager(
    stops: List<StopSummaryResponse>,
    placeDetails: Map<Long, PlaceDetailResponse>,
    loadingPlaceIds: Set<Long>,
    onPlaceSelected: (Long) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { stops.size })

    LaunchedEffect(pagerState.currentPage, stops) {
        stops.getOrNull(pagerState.currentPage)?.let { onPlaceSelected(it.spotId) }
    }

    // 장소 셀렉터: 페이지 하나 = 장소 하나. 좌우 옆 카드가 살짝 보이게 여백을 줘서
    // "가로로 넘긴다"는 게 시각적으로 드러나게 함.
    HorizontalPager(
        state = pagerState,
        contentPadding = PaddingValues(horizontal = 36.dp),
        pageSpacing = 10.dp,
        modifier = Modifier.fillMaxWidth()
    ) { page ->
        val stop = stops[page]
        PlaceSelectorChip(
            order = page + 1,
            stop = stop,
            selected = page == pagerState.currentPage
        )
    }

    Spacer(modifier = Modifier.height(6.dp))

    // 점 인디케이터 - 지금 몇 번째 장소를 보고 있는지
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        stops.forEachIndexed { index, _ ->
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (index == pagerState.currentPage) AccentBlue else LightGrayBorder)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    val currentSpotId = stops.getOrNull(pagerState.currentPage)?.spotId
    val currentPlace = currentSpotId?.let { placeDetails[it] }
    val isLoadingCurrent = currentSpotId != null && currentSpotId in loadingPlaceIds

    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
        when {
            isLoadingCurrent && currentPlace == null -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            }
            currentPlace != null -> {
                PlaceDetailCard(place = currentPlace)
            }
            else -> {
                Text(
                    text = "장소 정보를 불러오지 못했어요",
                    fontSize = 13.sp,
                    color = GrayText,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun PlaceSelectorChip(order: Int, stop: StopSummaryResponse, selected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) Color(0xFFE8F1FB) else GrayBg)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (selected) AccentBlue else Color(0xFFBBBBBB)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = order.toString(), color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stop.name,
            fontSize = 15.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.Black else GrayText,
            maxLines = 1
        )
    }
}

@Composable
private fun PlaceDetailCard(place: PlaceDetailResponse) {
    var showAllReviews by remember(place.spotId) { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // 장소 이미지
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFE8EEF5))
        ) {
            if (!place.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = place.imageUrl,
                    contentDescription = place.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(text = place.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(4.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = StarColor, modifier = Modifier.size(15.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (place.averageRating != null && place.averageRating > 0) {
                    "%.1f".format(place.averageRating)
                } else {
                    "리뷰 없음"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF333333)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "후기 ${place.reviewCount}개", fontSize = 13.sp, color = GrayText)
        }

        if (!place.description.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = place.description, fontSize = 13.sp, color = Color(0xFF555555), lineHeight = 19.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (place.reviews.isEmpty()) {
            Text(
                text = "아직 등록된 후기가 없어요",
                fontSize = 13.sp,
                color = GrayText,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            val visibleReviews = if (showAllReviews) place.reviews else place.reviews.take(COLLAPSED_REVIEW_COUNT)

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                visibleReviews.forEach { review ->
                    PlaceReviewCard(review = review)
                }
            }

            if (place.reviews.size > COLLAPSED_REVIEW_COUNT) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (showAllReviews) "후기 접기" else "후기 ${place.reviews.size}개 모두 보기",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAllReviews = !showAllReviews }
                        .padding(vertical = 10.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun PlaceReviewCard(review: PlaceReviewResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(GrayBg)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8EEF5))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = review.writerNickname, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (review.rating != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) { i ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (i < review.rating) StarColor else LightGrayBorder,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                    }
                }
            }
            if (!review.createdAt.isNullOrBlank()) {
                Text(text = review.createdAt.take(10), fontSize = 11.sp, color = GrayText)
            }
        }

        if (!review.reviewComment.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = review.reviewComment, fontSize = 13.sp, color = Color(0xFF333333))
        }

        if (!review.photoUrl.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            AsyncImage(
                model = review.photoUrl,
                contentDescription = "후기 사진",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(10.dp))
            )
        }
    }
}
