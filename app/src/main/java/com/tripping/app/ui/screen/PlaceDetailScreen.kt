package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tripping.app.data.response.PlaceDetailResponse
import com.tripping.app.data.response.PlaceReviewResponse
import com.tripping.app.data.response.RegisteredRouteCardResponse
import com.tripping.app.viewmodel.PlaceDetailViewModel

private val AccentBlue = Color(0xFF0074CE)
private val GrayText = Color(0xFF818181)
private val LightGrayBorder = Color(0xFFE0E0E0)
private val LightGrayBg = Color(0xFFF0F0F0)

/**
 * 장소 상세 조회 화면. RouteDetailScreen의 방문지 리스트를 탭하거나,
 * 탐색 화면에서 핑 하나를 눌렀을 때 이동해요.
 *
 * 등록된 루트 카드는 거리 계산 없이 "장소 개수"만 표시하고,
 * Ping 로그 후기는 사진 없이 별점+텍스트만 표시해요 (요청에 따라 단순화).
 */
@Composable
fun PlaceDetailScreen(
    placeId: Long,
    viewModel: PlaceDetailViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onRouteCardClick: (Long) -> Unit = {}
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var isSaved by remember { mutableStateOf(false) }

    LaunchedEffect(placeId) { viewModel.loadDetail(placeId) }

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
                PlaceDetailContent(
                    detail = detail!!,
                    isSaved = isSaved,
                    onBackClick = onBackClick,
                    onSaveClick = { isSaved = !isSaved }, // TODO: 실제 장소 저장(북마크) API 연동
                    onRouteCardClick = onRouteCardClick
                )
            }
        }
    }
}

@Composable
private fun PlaceDetailContent(
    detail: PlaceDetailResponse,
    isSaved: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onRouteCardClick: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ===== 상단 대표 사진 + 뒤로가기/저장/공유 아이콘 =====
        Box(modifier = Modifier.fillMaxWidth().height(280.dp)) {
            if (!detail.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = detail.imageUrl,
                    contentDescription = detail.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(modifier = Modifier.fillMaxSize().background(LightGrayBg))
            }

            RoundIconButton(
                icon = { Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로가기", tint = Color.Black) },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .clickable { onBackClick() }
            )

            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RoundIconButton(
                    icon = {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                            contentDescription = "저장",
                            tint = if (isSaved) AccentBlue else Color.Black
                        )
                    },
                    modifier = Modifier.clickable { onSaveClick() }
                )
                RoundIconButton(
                    icon = { Icon(Icons.Filled.Share, contentDescription = "공유", tint = Color.Black) }
                )
            }
        }

        // ===== 사진 아래 흰색 카드 (제목 + 통계 박스) =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .offset(y = (-20).dp)
                .padding(top = 24.dp, start = 20.dp, end = 20.dp)
        ) {
            Text(text = detail.name, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.Black)

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.LocationOn,
                    title = "이 장소가 등록된 핑 수",
                    value = "${detail.pingCount}",
                    subtitle = "이 장소가 기록된 횟수예요"
                )
                StatBox(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.AccessTime,
                    title = "인기 시간대",
                    value = detail.popularTimeSlot ?: "정보 없음",
                    subtitle = "방문객이 많이 몰리는 시간이에요"
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ===== 이 장소 통계 (지금은 장소 설명을 여기 넣음) =====
            Text(text = "이 장소 통계", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightGrayBg, RoundedCornerShape(16.dp))
                    .padding(20.dp)
            ) {
                Text(
                    text = detail.description?.takeIf { it.isNotBlank() } ?: "아직 등록된 장소 설명이 없어요",
                    fontSize = 15.sp,
                    color = if (detail.description.isNullOrBlank()) GrayText else Color.Black,
                    lineHeight = 22.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ===== 이 장소가 등록된 루트 =====
            Text(text = "이 장소가 등록된 루트", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (detail.registeredRoutes.isEmpty()) {
            Text(
                text = "아직 이 장소가 포함된 루트가 없어요",
                fontSize = 14.sp,
                color = GrayText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                detail.registeredRoutes.forEach { route ->
                    RouteCard(route = route, onClick = { onRouteCardClick(route.actualRouteId) })
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
        HorizontalDivider(color = LightGrayBorder, thickness = 8.dp)
        Spacer(modifier = Modifier.height(20.dp))

        // ===== Ping 로그 후기 =====
        Text(
            text = "Ping 로그 후기",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (detail.reviews.isEmpty()) {
            Text(
                text = "아직 후기가 없어요",
                fontSize = 14.sp,
                color = GrayText,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        } else {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                detail.reviews.forEach { review ->
                    ReviewRow(review = review)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun RoundIconButton(
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .background(Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        icon()
    }
}

@Composable
private fun StatBox(
    modifier: Modifier = Modifier,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    subtitle: String
) {
    Column(
        modifier = modifier
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, LightGrayBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(text = title, fontSize = 13.sp, color = GrayText)
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = subtitle, fontSize = 12.sp, color = GrayText)
    }
}

@Composable
private fun RouteCard(
    route: RegisteredRouteCardResponse,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .width(160.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(LightGrayBg)
        ) {
            if (!route.photoUrl.isNullOrBlank()) {
                AsyncImage(
                    model = route.photoUrl,
                    contentDescription = route.themeName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = route.themeName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 1)
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = "${route.placeCount}곳", fontSize = 13.sp, color = GrayText)
    }
}

@Composable
private fun ReviewRow(review: PlaceReviewResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, LightGrayBorder, RoundedCornerShape(14.dp))
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = review.writerNickname, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            if (review.rating != null) {
                Row {
                    repeat(5) { index ->
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < review.rating) Color(0xFFFFC107) else LightGrayBorder,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
        if (!review.reviewComment.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = review.reviewComment, fontSize = 14.sp, color = Color.Black, lineHeight = 20.sp)
        }
    }
}