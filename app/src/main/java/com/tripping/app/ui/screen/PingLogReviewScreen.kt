// [파일 설명] 핑 하나(방문 장소)에 대한 후기 작성 화면 UI. 별점 및 텍스트 후기, 최대 3개 #해시태그 자동 추출 기능을 포함한 화면 (등록 버튼 단일화 버전).
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
private val StarColor = Color(0xFFFFC107) // 별점 색상 (골드)

private const val MAX_REVIEW_LENGTH = 500

@Composable
fun PingLogReviewScreen(
    placeName: String,
    onBackClick: () -> Unit,
    onSubmit: (content: String, rating: Int, tags: List<String>) -> Unit,
    onSubmitAndNext: (content: String, rating: Int, tags: List<String>) -> Unit // 시그니처 유지 (필요시 내부에서 onSubmit 호출)
) {
    var reviewText by remember { mutableStateOf("") }
    var rating by remember { mutableIntStateOf(5) } // 기본 별점 5점 설정

    // 💡 텍스트 본문에서 #해시태그를 추출하고 최대 3개까지만 제한하는 함수
    val extractHashtags: (String) -> List<String> = { text ->
        val regex = "#([\\w가-힣]+)".toRegex()
        regex.findAll(text)
            .map { it.groupValues[1] } // '#' 기호를 뺀 순수 단어만 추출
            .distinct()                // 중복 태그 제거
            .take(3)                   // 최대 3개까지만 제한
            .toList()
    }

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
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            // ===== 별점 선택 영역 =====
            Text(text = "별점", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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

            // ===== 텍스트 후기 영역 (본문에 #태그 최대 3개 입력 가능) =====
            Text(text = "Ping 로그 후기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "본문에 #을 붙여 해시태그를 최대 3개까지 작성할 수 있어요",
                fontSize = 11.sp,
                color = GrayText
            )
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = reviewText,
                onValueChange = { if (it.length <= MAX_REVIEW_LENGTH) reviewText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp) // 높이를 조금 더 넓게 조정
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = {
                    Text(
                        text = "이 장소에서의 후기를 남겨보세요\n(예: 커피도 맛있고 #분위기좋은 #카페 추천해요)",
                        color = GrayText,
                        fontSize = 13.sp
                    )
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = GrayBg,
                    unfocusedContainerColor = GrayBg,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "${reviewText.length} / $MAX_REVIEW_LENGTH",
                    fontSize = 11.sp,
                    color = GrayText
                )
            }
        }

        // ===== 하단 단일 등록 버튼 (넓고 시원하게 배치) =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BluePrimary)
                .clickable {
                    val tags = extractHashtags(reviewText)
                    onSubmit(reviewText, rating, tags)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "등록",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}