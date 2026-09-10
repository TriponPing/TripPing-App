// [파일 설명] 핑 하나(방문 장소)에 대한 후기 작성 화면 UI. 별점, 텍스트 후기, 그리고 칩(chip) 형태의 태그 입력 UI를 포함한 화면.
package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
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
private val TagChipBg = Color(0xFFE9EEF8)

private const val MAX_REVIEW_LENGTH = 500
private const val MAX_TAGS = 3

@Composable
fun PingLogReviewScreen(
    placeName: String,
    initialRating: Int? = null,
    initialContent: String? = null,
    initialTags: List<String> = emptyList(),
    isEditMode: Boolean = false,
    onBackClick: () -> Unit,
    onSubmit: (content: String, rating: Int, tags: List<String>) -> Unit,
    onSubmitAndNext: (content: String, rating: Int, tags: List<String>) -> Unit
) {
    var reviewText by remember { mutableStateOf(initialContent ?: "") }
    var rating by remember { mutableIntStateOf(initialRating ?: 5) } // 기존 후기 있으면 그 값, 없으면 기본 5점

    // 👈 새로 추가: 칩 형태 태그 입력용 상태 (기존 태그가 있으면 미리 채워둠)
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(initialTags) }

    // 텍스트 본문에서 #해시태그도 보조로 추출 (직접 입력한 칩 태그와 합쳐서 최대 3개)
    val extractHashtagsFromBody: (String) -> List<String> = { text ->
        val regex = "#([\\w가-힣]+)".toRegex()
        regex.findAll(text).map { it.groupValues[1] }.toList()
    }

    // 👈 수정: "#맛집 #분위기"처럼 한 번에 여러 개를 입력해도 한 덩어리로 등록되지 않고
    // #이나 공백 기준으로 쪼개서 각각 따로 태그로 추가되게 함
    fun addTag(raw: String) {
        val candidates = raw.split(Regex("[#\\s]+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }

        for (candidate in candidates) {
            if (tags.size >= MAX_TAGS) break
            if (tags.contains(candidate)) continue
            tags = tags + candidate
        }
        tagInput = ""
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

            // ===== 👈 새로 추가: 태그 입력 영역 (칩 형태) =====
            Text(text = "태그", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "최대 ${MAX_TAGS}개까지 추가할 수 있어요",
                fontSize = 11.sp,
                color = GrayText
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = tagInput,
                    onValueChange = { tagInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp)),
                    enabled = tags.size < MAX_TAGS,
                    placeholder = {
                        Text(
                            text = if (tags.size < MAX_TAGS) "예: 분위기좋은" else "최대 개수에 도달했어요",
                            color = GrayText,
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { addTag(tagInput) }),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = GrayBg,
                        unfocusedContainerColor = GrayBg,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (tags.size < MAX_TAGS) BluePrimary else GrayBg)
                        .clickable(enabled = tags.size < MAX_TAGS) { addTag(tagInput) }
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "추가", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            if (tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tags.forEach { tag ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(TagChipBg)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "#$tag", fontSize = 13.sp, color = BluePrimary, fontWeight = FontWeight.Medium)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "×",
                                fontSize = 14.sp,
                                color = BluePrimary,
                                modifier = Modifier.clickable { tags = tags.filterNot { it == tag } }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ===== 텍스트 후기 영역 =====
            Text(text = "Ping 로그 후기", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = reviewText,
                onValueChange = { if (it.length <= MAX_REVIEW_LENGTH) reviewText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = {
                    Text(
                        text = "이 장소에서의 후기를 남겨보세요",
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

        // ===== 하단 단일 등록 버튼 =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(BluePrimary)
                .clickable {
                    // 직접 추가한 태그 칩 + 본문 속 #해시태그를 합쳐서 최대 3개까지만 전송
                    val finalTags = (tags + extractHashtagsFromBody(reviewText))
                        .distinct()
                        .take(MAX_TAGS)
                    onSubmit(reviewText, rating, finalTags)
                },
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEditMode) "수정" else "등록",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )
        }
    }
}