// [파일 설명] 핑 하나(방문 장소)에 대한 후기 작성 화면 UI. 텍스트 후기를 입력하고 등록/수정 API를 호출하는 화면. (사진 기능은 백엔드 API 미지원으로 제외)
package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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

// ===== 색상 =====
private val BluePrimary = Color(0xFF4A72C4)
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)

private const val MAX_REVIEW_LENGTH = 500

@Composable
fun PingLogReviewScreen(
    placeName: String,
    onBackClick: () -> Unit,
    onSubmit: (content: String) -> Unit,
    onSubmitAndNext: (content: String) -> Unit
) {
    var reviewText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 상단바: 뒤로가기 + 장소명
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
            Text(text = placeName, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp)
        ) {
            Text(text = "Ping 로그 후기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(10.dp))

            TextField(
                value = reviewText,
                onValueChange = { if (it.length <= MAX_REVIEW_LENGTH) reviewText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = { Text(text = "이 장소에서의 후기를 남겨보세요", color = GrayText, fontSize = 13.sp) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GrayBg,
                    unfocusedContainerColor = GrayBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${reviewText.length} / $MAX_REVIEW_LENGTH",
                fontSize = 11.sp,
                color = GrayText,
                modifier = Modifier.align(Alignment.End)
            )
        }

        // ===== 하단 버튼 2종 =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 그냥 등록 (여기서 끝)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(GrayBg)
                    .clickable { onSubmit(reviewText) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "등록", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.Black)
            }

            // 등록 후 다음 장소로 이어서 작성
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(BluePrimary)
                    .clickable { onSubmitAndNext(reviewText) },
                contentAlignment = Alignment.Center
            ) {
                Text(text = "등록 후 다음 장소 작성하기", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)
            }
        }
    }
}