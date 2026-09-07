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
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.KeyboardArrowLeft

private val AccentBlue = Color(0xFF0074CE)
private val GrayText = Color(0xFF818181)
private val LightGrayBorder = Color(0xFFE0E0E0)
private val LightGrayBg = Color(0xFFF0F0F0)
private val TagChipBg = Color(0xFFE3F2FD)

// ===== 데이터 모델 =====

data class RoutePlaceItem(
    val order: Int,
    val name: String,
    val tags: List<String>
)

data class RecommendedRoute(
    val id: String,
    val label: String,           // "추천1"
    val places: List<RoutePlaceItem>
)

/**
 * 루트 생성 2단계: 사용자가 입력한 정보를 바탕으로 추천 루트를 보여주는 화면.
 *
 * @param routes 추천 루트 목록 (탭으로 전환)
 * @param onBackClick "뒤로가기" 클릭 시 (1단계 정보 입력 화면으로)
 * @param onApplyClick "적용" 클릭 시, 선택된 추천 루트를 그대로 사용
 * @param onSkipClick "추천 안받고 다음으로" 클릭 시, 추천을 사용하지 않고 다음 단계로
 * @param onPlaceClick 리스트의 개별 장소를 클릭했을 때 (상세/수정 등)
 */
@Composable
fun RouteRecommendScreen(
    routes: List<RecommendedRoute>,
    onBackClick: () -> Unit = {},
    onApplyClick: (RecommendedRoute) -> Unit = {},
    onSkipClick: () -> Unit = {},
    onPlaceClick: (RecommendedRoute, RoutePlaceItem) -> Unit = { _, _ -> }
) {
    var selectedIndex by remember { mutableStateOf(0) }
    val selectedRoute = routes.getOrNull(selectedIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState())
    ) {
        // ===== 상단 타이틀 =====
        Text(
            text = "루트",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
        )

        // ===== 뒤로가기 =====
        BackButtonRow(onClick = onBackClick)
        HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)

        // ===== 단계 표시기 (2단계 활성) =====
        RecommendStepIndicator(currentStep = 2)

        Spacer(modifier = Modifier.height(24.dp))

        // ===== "추천루트" 타이틀 =====
        Text(
            text = "추천루트",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 추천 탭 (추천1, 추천2, ...) =====
        // ===== 추천 탭 (추천1~4, 화면 너비에 맞춰 균등 분할) =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            routes.forEachIndexed { index, route ->
                RecommendTab(
                    text = route.label,
                    selected = index == selectedIndex,
                    modifier = Modifier.weight(1f),
                    onClick = { selectedIndex = index }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ===== 선택된 루트의 장소 리스트 =====
        if (selectedRoute != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, LightGrayBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                selectedRoute.places.forEachIndexed { index, place ->
                    PlaceRow(
                        place = place,
                        onClick = { onPlaceClick(selectedRoute, place) }
                    )
                    if (index < selectedRoute.places.size - 1) {
                        HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 장소 개수 요약 =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .border(1.dp, LightGrayBorder, RoundedCornerShape(12.dp))
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = null, tint = AccentBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "장소 ${selectedRoute.places.size}개",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== 적용 버튼 =====
            Button(
                onClick = { onApplyClick(selectedRoute) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(text = "적용", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
        } else {
            // 추천 루트가 없을 때 (예: API 실패, 빈 목록)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "추천 루트를 불러올 수 없어요", fontSize = 14.sp, color = GrayText)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // ===== 추천 안받고 다음으로 버튼 =====
        Button(
            onClick = onSkipClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text(text = "추천 안받고 다음으로", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PlaceRow(
    place: RoutePlaceItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 순번 원형 배지
        Box(
            modifier = Modifier
                .size(26.dp)
                .background(AccentBlue, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = place.order.toString(),
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = place.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            if (place.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    place.tags.forEach { tag ->
                        Box(
                            modifier = Modifier
                                .background(TagChipBg, RoundedCornerShape(20.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = tag, fontSize = 12.sp, color = AccentBlue, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null, tint = GrayText)
    }
}

@Composable
private fun RecommendTab(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) AccentBlue else LightGrayBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .background(Color.White, RoundedCornerShape(24.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (selected) AccentBlue else Color.Black,
            maxLines = 1
        )
    }
}

@Composable
private fun RecommendStepIndicator(currentStep: Int) {
    val steps = listOf("정보 입력", "추천 확인", "완료")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isDone = stepNumber < currentStep

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isActive || isDone) AccentBlue else Color(0xFFE0E0E0),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stepNumber.toString(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) AccentBlue else GrayText
                )
            }

            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 20.dp),
                    color = if (stepNumber < currentStep) AccentBlue else Color(0xFFE0E0E0),
                    thickness = 2.dp
                )
            }
        }
    }
}



// ===== 프리뷰용 샘플 데이터 =====

private fun sampleRoutes(): List<RecommendedRoute> = listOf(
    RecommendedRoute(
        id = "1",
        label = "추천1",
        places = listOf(
            RoutePlaceItem(1, "한강 힐링 산책 루트", listOf("한강공원", "산책", "자연")),
            RoutePlaceItem(2, "암사동 역사 탐방 루트", listOf("역사", "문화", "전통")),
            RoutePlaceItem(3, "도심 카페 감성 루트", listOf("카페", "감성", "핫플")),
            RoutePlaceItem(4, "야경 드라이브 루트", listOf("드라이브", "야경", "전망"))
        )
    ),
    RecommendedRoute(
        id = "2",
        label = "추천2",
        places = listOf(
            RoutePlaceItem(1, "성수동 카페 투어", listOf("카페", "감성")),
            RoutePlaceItem(2, "서울숲 산책", listOf("자연", "산책"))
        )
    )
)

@Preview(showBackground = true, heightDp = 1400)
@Composable
private fun RouteRecommendScreenPreview() {
    MaterialTheme {
        RouteRecommendScreen(routes = sampleRoutes())
    }
}

 @Composable
 private fun BackButtonRow(onClick: () -> Unit) {
      Row(
          modifier = Modifier
              .fillMaxWidth()
              .clickable { onClick() }
              .padding(horizontal = 20.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically
      ) {
          Icon(
              imageVector = Icons.Filled.KeyboardArrowLeft,
              contentDescription = "뒤로가기",
              tint = Color.Black,
              modifier = Modifier.size(22.dp)
          )
          Spacer(modifier = Modifier.width(2.dp))
          Text(text = "뒤로가기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
      }
}