package com.tripping.app.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripping.app.R

// ===== 여행 카드 - 마이페이지, 다녀온 여행 자세히보기 등 여러 화면에서 재사용 =====

val ColorTextPrimary = Color(0xFF000000)
val ColorTextSecondary = Color(0xFF818181)
val ColorCardBorder = Color(0xFFD9D9D9)

data class MyPageTripCard(
    val title: String,
    val dateRange: String,
    // 참고: 백엔드(GET /users/me/trips 등)가 핑 개수/소요시간을 안 내려줘서 nullable로 둠.
    // 필요하면 여행 상세(GET /users/me/trips/{tripId}) 따로 호출해서 채워야 함.
    val pingCount: Int? = null,
    val duration: String? = null,
    val isSaved: Boolean = false,
    val routeId: Long? = null // 저장한 루트 취소(DELETE /routes/{routeId}/saved) 호출할 때 사용
)

@Composable
fun TripCard(trip: MyPageTripCard, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .border(1.dp, ColorCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text = trip.title, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = trip.dateRange, fontSize = 12.sp, color = ColorTextSecondary)
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoutePreviewDots(modifier = Modifier.weight(1f), count = trip.pingCount ?: 4)
            if (trip.pingCount != null || trip.duration != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Column(horizontalAlignment = Alignment.End) {
                    trip.pingCount?.let {
                        Text(text = "핑 ${it}개", fontSize = 12.sp, color = ColorTextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    trip.duration?.let {
                        Text(text = it, fontSize = 12.sp, color = ColorTextSecondary)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TripCardPreview() {
    TripCard(
        trip = MyPageTripCard("성수 나들이 코스", "2026.08.13 ~ 08.14", 6, "1일 1시간 6분")
    )
}

// 여행 경로를 핑 아이콘(점) + 선으로 표현. 피그마 디자인 맞춰서 점은 blueping.png 아이콘 사용.
// count = 실제 방문 장소 개수. 4개 넘으면 4개까지만 보여주고 잘라냄.
@Composable
fun RoutePreviewDots(modifier: Modifier = Modifier, count: Int = 4) {
    val dotCount = count.coerceIn(1, 4)
    Row(
        modifier = modifier.height(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val lineColors = (0 until dotCount).map { index ->
            if (index < (dotCount + 1) / 2) Color(0xFF2ECC71) else Color(0xFF3B9AE1)
        }
        lineColors.forEachIndexed { index, color ->
            Image(
                painter = painterResource(id = R.drawable.blueping),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
            if (index != lineColors.lastIndex) {
                Box(
                    modifier = Modifier
                        .height(2.dp)
                        .width(16.dp)
                        .background(color)
                )
            }
        }
    }
}
