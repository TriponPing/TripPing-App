package com.tripping.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripping.app.data.response.PlaceSearchResponse

@Composable
fun PlaceInfoCard(
    place: PlaceSearchResponse,
    popularTime: String? = null, // 아직 API 없음 -> null이면 "-" 표시
    pingCount: Int? = null,      // 아직 API 없음 -> null이면 "-" 표시
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(166.dp),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = place.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black
                    )
                    Text(
                        text = com.tripping.app.viewmodel.PlaceCategory.labelOf(place.category),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF818181)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF818181),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "가장 많이 가는 시간",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF818181)
                        )
                        Text(
                            text = popularTime ?: "-",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.HorizontalDivider(color = Color(0xFF818181).copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF818181),
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "핑 개수",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF818181)
                        )
                        Text(
                            text = pingCount?.toString() ?: "-",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 장소 이미지 (imageUrl 실제 로딩은 나중에 Coil 라이브러리로 교체)
            Box(
                modifier = Modifier
                    .size(98.dp)
                    .background(Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
            )
        }
    }
}