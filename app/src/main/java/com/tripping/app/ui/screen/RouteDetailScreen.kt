package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.tripping.app.data.response.PopularTripDetailResponse
import com.tripping.app.data.response.StopSummaryResponse
import com.tripping.app.viewmodel.PopularTripDetailViewModel

private val AccentBlue = Color(0xFF0074CE)
private val AccentGreen = Color(0xFF4CAF50)
private val GrayText = Color(0xFF818181)
private val LightGrayBorder = Color(0xFFE0E0E0)
private val PopularBadgeBg = Color(0xFFFFE0E3)
private val PopularBadgeText = Color(0xFFE5566A)

/**
 * 탐색 화면에서 루트(핑 사이 연결선)를 눌렀을 때 이동하는 상세 페이지.
 *
 * 소요시간/거리, 리뷰 텍스트, "인기" 판정 기준은 백엔드에 아직 없어서:
 * - 소요시간/거리는 아예 뺐고 (계산 어려움)
 * - "인기" 뱃지는 이 API 자체가 "이번주 인기 여행" 목록에서 온 거라 조건 없이 항상 표시
 * - 방문 만족도(★)/등록된 루트 수는 실제 연동됨 (PingLog 평균 평점 + spot별 루트 카운트)
 */
@Composable
fun RouteDetailScreen(
    routeId: Long,
    viewModel: PopularTripDetailViewModel = viewModel(),
    onBackClick: () -> Unit = {},
    onStopClick: (Long) -> Unit = {}
) {
    val detail by viewModel.detail.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()

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
                RouteDetailContent(
                    detail = detail!!,
                    isSaved = isSaved,
                    onBackClick = onBackClick,
                    onStopClick = onStopClick,
                    onSaveClick = {
                        viewModel.toggleSaveRoute(routeId)
                    }
                )
            }
        }
    }
}

@Composable
private fun RouteDetailContent(
    detail: PopularTripDetailResponse,
    isSaved: Boolean,
    onBackClick: () -> Unit,
    onStopClick: (Long) -> Unit,
    onSaveClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // ===== 상단: 뒤로가기 + 저장 아이콘 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onBackClick() }
            ) {
                Icon(Icons.Filled.KeyboardArrowLeft, contentDescription = "뒤로가기", tint = Color.Black)
                Text(text = "뒤로가기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            }
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = "루트 저장",
                tint = AccentBlue,
                modifier = Modifier
                    .size(26.dp)
                    .clickable { onSaveClick() }
            )
        }

        // ===== 작성자 프로필 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(Color(0xFFEEEEEE), CircleShape),
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
                            modifier = Modifier.fillMaxSize().background(Color.Transparent, CircleShape)
                        )
                    } else {
                        AsyncImage(
                            model = writerProfileImage,
                            contentDescription = "프로필 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize().background(Color.Transparent, CircleShape)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${detail.writerNickname}의 루트",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(PopularBadgeBg, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(text = "인기", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PopularBadgeText)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "@${detail.writerNickname}", fontSize = 13.sp, color = GrayText)
                if (!detail.writerLevel.isNullOrBlank()) {
                    Text(text = "Lv. ${detail.writerLevel}", fontSize = 13.sp, color = GrayText)
                }
            }
        }

        HorizontalDivider(color = LightGrayBorder, thickness = 6.dp)

        // ===== 방문 경로 텍스트 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            detail.stops.forEachIndexed { index, stop ->
                Text(text = stop.name, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                if (index < detail.stops.size - 1) {
                    Text(text = "  →  ", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = GrayText)
                }
            }
        }

        // ===== 저장 수 =====
        Text(
            text = "${formatCount(detail.savedCount)}명이 이 루트를 저장했어요",
            fontSize = 14.sp,
            color = GrayText,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 방문 장소 개수 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "방문 장소", fontSize = 13.sp, color = GrayText)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = "${detail.placeCount}곳", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
        HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)

        // ===== 방문 장소 리스트 =====
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            detail.stops.forEachIndexed { index, stop ->
                StopRow(
                    order = index + 1,
                    isLast = index == detail.stops.lastIndex,
                    stop = stop,
                    onClick = { onStopClick(stop.spotId) }
                )
                if (index < detail.stops.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ===== 이 루트 저장하기 버튼 =====
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Icon(
                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSaved) "저장됨" else "이 루트 저장하기",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun StopRow(
    order: Int,
    isLast: Boolean,
    stop: StopSummaryResponse,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(if (isLast) AccentGreen else AccentBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = order.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = stop.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = GrayText, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "등록된 루트  ${stop.registeredRouteCount}", fontSize = 13.sp, color = GrayText)

                Spacer(modifier = Modifier.width(12.dp))

                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFC107), modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (stop.averageRating != null && stop.averageRating > 0) {
                        "방문 만족도  %.1f".format(stop.averageRating)
                    } else {
                        "리뷰 없음"
                    },
                    fontSize = 13.sp,
                    color = GrayText
                )
            }
        }
    }
}

// 24000 -> "2.4만" 형태로 변환
private fun formatCount(count: Long): String {
    return when {
        count >= 10000 -> "%.1f만".format(count / 10000.0)
        count >= 1000 -> "%.1f천".format(count / 1000.0)
        else -> count.toString()
    }
}