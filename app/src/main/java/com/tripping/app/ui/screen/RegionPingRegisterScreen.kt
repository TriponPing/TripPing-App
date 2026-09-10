// [파일 설명] 지역핑 등록 화면. Ping "로그" 탭에서 "지역핑 등록하기"를 누르면, 장소 선택(PingPlaceSearchScreen 재사용)
// 다음 단계로 이 화면이 뜸. 이미 고른 장소의 이름/위치를 보여주고 평점 + 후기를 남기면 등록됨.
package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.overlay.Marker
import com.tripping.app.ui.component.NaverMapContainer
import com.tripping.app.ui.component.applyPingIcon

private val BluePrimary = Color(0xFF4A72C4)
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)
private val StarColor = Color(0xFFFFC107)

private const val MAX_REVIEW_LENGTH = 500

@Composable
fun RegionPingRegisterScreen(
    regionName: String,
    spotName: String,
    address: String,
    latitude: Double,
    longitude: Double,
    isSubmitting: Boolean = false,
    errorMessage: String? = null,
    onBackClick: () -> Unit,
    onSubmit: (rating: Int, reviewComment: String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단바: 뒤로가기 + 지역명
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "‹",
                fontSize = 22.sp,
                color = Color.Black,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(end = 12.dp)
            )
            Text(text = regionName, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // ===== 위치 미리보기 지도 =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(GrayBg)
            ) {
                NaverMapContainer(modifier = Modifier.fillMaxSize()) { naverMap ->
                    val position = LatLng(latitude, longitude)
                    naverMap.moveCamera(CameraUpdate.scrollAndZoomTo(position, 15.0))
                    Marker().apply {
                        this.position = position
                        applyPingIcon()
                        map = naverMap
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ===== 이름 =====
            Text(text = "이름", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GrayBg)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(text = spotName, fontSize = 14.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ===== 위치 =====
            Text(text = "위치", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(GrayBg)
                    .padding(horizontal = 14.dp, vertical = 14.dp)
            ) {
                Text(
                    text = address.ifBlank { "주소 정보 없음" },
                    fontSize = 14.sp,
                    color = Color.Black
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 평점 =====
            Text(text = "평점", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Text(
                        text = if (i <= rating) "★" else "☆",
                        fontSize = 28.sp,
                        color = if (i <= rating) StarColor else GrayText,
                        modifier = Modifier
                            .clickable { rating = i }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== Ping 로그 후기 =====
            Text(text = "Ping 로그 후기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = reviewText,
                onValueChange = { if (it.length <= MAX_REVIEW_LENGTH) reviewText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = {
                    Text(text = "이 장소에 대한 지역핑 후기를 남겨보세요", color = GrayText, fontSize = 13.sp)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GrayBg,
                    unfocusedContainerColor = GrayBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(text = "${reviewText.length} / $MAX_REVIEW_LENGTH", fontSize = 11.sp, color = GrayText)
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, fontSize = 12.sp, color = Color(0xFFE74C3C))
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        // ===== 등록 버튼 =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BluePrimary)
                .clickable(enabled = !isSubmitting) { onSubmit(rating, reviewText) },
            contentAlignment = Alignment.Center
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
            } else {
                Text(text = "등록", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
            }
        }
    }
}
