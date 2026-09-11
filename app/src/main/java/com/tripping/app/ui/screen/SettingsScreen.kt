package com.tripping.app.ui.screen

import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.data.response.BadgeResponse
import com.tripping.app.viewmodel.MyPageViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// ===== 마이페이지 설정 화면 (마이페이지 오른쪽 위 톱니바퀴) =====
// 프로필(닉네임/레벨)은 실제 API(GET·PATCH /users/me) 연동됨.
// 뱃지("뱃지"/"꺼낼 뱃지")도 실제 API(GET /users/me/badges, PUT /users/me/badges/featured) 연동됨.
// 뱃지 카탈로그는 10종 고정(백엔드 BadgeCatalog 참고) - 달성 여부(earned)는 저장된 값이 아니라
// 실제 활동 데이터(핑 개수, 완주 여행, 지역핑, 태그, 저장 등) 기준으로 매번 새로 계산됨.
// 못 딴 뱃지는 잠금 상태로 보여주고, 눌러보면 달성 조건(퀘스트)을 안내함.

private val ColorBackground = Color(0xFFF8F8FC)
private val ColorAccentBlue = Color(0xFF0074CE) // 앱 전반에서 쓰는 포인트 블루
private val ColorBadgeCircleBg = Color(0xFFEFF3F8)
private val ColorFeaturedTrayBg = Color(0xFFE3E3E3)

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogoutConfirmed: () -> Unit = {},
    viewModel: MyPageViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadProfile()
        viewModel.loadBadges()
    }
    val profile by viewModel.profile.collectAsState()
    val badges by viewModel.badges.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showNicknameDialog by remember { mutableStateOf(false) }
    var nicknameInput by remember { mutableStateOf("") }
    var questBadgeInfo by remember { mutableStateOf<BadgeResponse?>(null) } // 잠긴 뱃지 눌렀을 때 조건 안내용

    // 👈 수정: 갤러리에서 고른 사진이 로컬 미리보기로만 남고 실제로 저장/적용이 안 되던 버그.
    // 아직 별도 이미지 업로드 API가 없어서, 사진을 리사이즈+압축한 뒤 base64 데이터 URI로
    // 인코딩해서 PATCH /users/me의 profileImage 문자열로 그대로 저장함.
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var pickedProfileImage by remember { mutableStateOf<ImageBitmap?>(null) }
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                if (bitmap != null) {
                    val dataUri = encodeProfileImage(bitmap)
                    withContext(Dispatchers.Main) {
                        pickedProfileImage = bitmap.asImageBitmap() // 업로드 반영 전까지 바로 보이는 미리보기
                        viewModel.updateProfileImage(dataUri)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorBackground)
    ) {
        // 상단바 - 뒤로가기만 단독으로 (다른 상세 화면들이랑 같은 스타일)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "‹",
                fontSize = 20.sp,
                color = ColorTextPrimary,
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(end = 8.dp, top = 2.dp, bottom = 2.dp)
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // ===== 프로필 =====
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box {
                    // 아바타 (TODO: 실제 프로필 이미지로 교체/업로드 - GET·PATCH /users/me의 profileImage)
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8EEF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        // 👈 수정: decodeProfileImage()가 매 recomposition마다(예: 닉네임 입력창에 한 글자
                        // 칠 때마다) 다시 실행돼서 base64 디코딩+이미지 압축해제를 반복하며 메인 스레드가
                        // 렉먹던 문제 - 한글처럼 여러 키 입력이 짧은 시간 안에 조합돼야 하는 IME 입력이
                        // 이 렉 때문에 끊겨서 아예 안 써지는 것처럼 보였음. remember로 캐싱해서 profile
                        // 값이 실제로 바뀔 때만 다시 디코딩하도록 수정.
                        // 방금 고른 사진(업로드 반영 전 즉시 미리보기)이 있으면 그걸, 없으면 서버에 저장된 사진을 보여줌
                        val decodedServerImage = remember(profile?.profileImage) { decodeProfileImage(profile?.profileImage) }
                        val displayedImage = pickedProfileImage ?: decodedServerImage
                        if (displayedImage != null) {
                            Image(
                                bitmap = displayedImage,
                                contentDescription = "프로필 사진",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                            )
                        } else {
                            Text(text = "👤", fontSize = 40.sp)
                        }
                    }
                    // 프로필 사진 수정 - 텍스트 대신 사진 오른쪽 아래에 연필 아이콘으로. 누르면 갤러리(사진 선택기) 열림.
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = "프로필 사진 수정",
                            tint = ColorTextPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(11.dp))
                            .background(ColorAccentBlue)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = profile?.level?.let { "Lv.$it" } ?: "-",
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = profile?.nickname ?: "불러오는 중...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "수정",
                        fontSize = 12.sp,
                        color = ColorTextSecondary,
                        modifier = Modifier.clickable {
                            nicknameInput = profile?.nickname ?: ""
                            showNicknameDialog = true
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== 뱃지 - 전체 카탈로그를 항상 보여주되, 아직 못 딴 뱃지는 잠금(흑백+자물쇠)으로 표시해서
            // 어떻게 하면 딸 수 있는지(퀘스트) 미리 보여줌. 딴 뱃지만 눌러서 "꺼낼 뱃지"로 켜고 끌 수 있음. =====
            Text(text = "뱃지", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            if (badges.isEmpty()) {
                Text(text = "불러오는 중...", fontSize = 12.sp, color = ColorTextSecondary)
            } else {
                if (badges.none { it.earned }) {
                    Text(
                        text = "아직 획득한 뱃지가 없어요 · 아래 뱃지를 눌러 조건을 확인해보세요",
                        fontSize = 12.sp,
                        color = ColorTextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    badges.forEach { badge ->
                        BadgeItem(
                            badge = badge,
                            onClick = {
                                if (badge.earned) {
                                    viewModel.toggleFeaturedBadge(badge.code)
                                } else {
                                    questBadgeInfo = badge
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== 꺼낼 뱃지 - 마이페이지 프로필 영역에 실제로 보여줄 뱃지. 눌러서 빼면 위 "뱃지" 목록 체크도 같이 풀림 =====
            Text(text = "꺼낼 뱃지", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorFeaturedTrayBg)
                    .padding(16.dp)
            ) {
                val featuredBadges = badges.filter { it.featured }
                if (featuredBadges.isEmpty()) {
                    Text(
                        text = if (badges.isEmpty()) {
                            "아직 뱃지가 없어요"
                        } else {
                            "위 뱃지 목록에서 눌러서 대표 뱃지로 꺼내보세요"
                        },
                        fontSize = 12.sp,
                        color = ColorTextSecondary
                    )
                } else {
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        featuredBadges.forEach { badge ->
                            BadgeItem(
                                badge = badge,
                                onClick = { viewModel.toggleFeaturedBadge(badge.code) }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "로그아웃",
                fontSize = 13.sp,
                color = ColorTextSecondary,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable { showLogoutDialog = true }
                    .padding(vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            text = {
                Text(text = "로그아웃 하시겠습니까?", fontSize = 15.sp, color = ColorTextPrimary)
            },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogoutConfirmed()
                }) {
                    Text(text = "확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(text = "취소")
                }
            }
        )
    }

    questBadgeInfo?.let { badge ->
        AlertDialog(
            onDismissRequest = { questBadgeInfo = null },
            title = { Text(text = "${badge.emoji} ${badge.label}", fontSize = 16.sp, fontWeight = FontWeight.Bold) },
            text = { Text(text = badge.conditionDesc, fontSize = 14.sp, color = ColorTextSecondary) },
            confirmButton = {
                TextButton(onClick = { questBadgeInfo = null }) {
                    Text(text = "확인")
                }
            }
        )
    }

    if (showNicknameDialog) {
        AlertDialog(
            onDismissRequest = { showNicknameDialog = false },
            text = {
                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    singleLine = true,
                    label = { Text("닉네임") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val trimmed = nicknameInput.trim()
                    if (trimmed.isNotEmpty()) {
                        viewModel.updateNickname(trimmed)
                    }
                    showNicknameDialog = false
                }) {
                    Text(text = "확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNicknameDialog = false }) {
                    Text(text = "취소")
                }
            }
        )
    }
}

@Composable
private fun BadgeItem(badge: BadgeResponse, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .clickable { onClick() }
    ) {
        Box {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ColorBadgeCircleBg)
                    .alpha(if (badge.earned) 1f else 0.35f), // 못 딴 뱃지는 흐리게(잠금) 표시
                contentAlignment = Alignment.Center
            ) {
                Text(text = badge.emoji, fontSize = 20.sp)
            }
            if (badge.featured) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "꺼낸 뱃지",
                    tint = ColorAccentBlue,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            } else if (!badge.earned) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "미획득 뱃지",
                    tint = ColorTextSecondary,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = badge.label,
            fontSize = 10.sp,
            color = if (badge.earned) ColorTextSecondary else ColorTextSecondary.copy(alpha = 0.5f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            maxLines = 1
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    SettingsScreen()
}

// 👈 새로 추가: 프로필 사진 업로드 API가 따로 없어서, 갤러리에서 고른 사진을 리사이즈+압축해
// base64 데이터 URI 문자열로 만들어 PATCH /users/me에 그대로 저장하기 위한 인코더.
// (마이페이지 프로필 아바타에서도 decodeProfileImage()로 이 문자열을 그대로 되돌림)
private const val PROFILE_IMAGE_MAX_SIZE = 256

internal fun encodeProfileImage(bitmap: android.graphics.Bitmap): String {
    val scale = minOf(1f, PROFILE_IMAGE_MAX_SIZE.toFloat() / maxOf(bitmap.width, bitmap.height))
    val resized = if (scale < 1f) {
        android.graphics.Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).toInt().coerceAtLeast(1),
            (bitmap.height * scale).toInt().coerceAtLeast(1),
            true
        )
    } else {
        bitmap
    }
    val outputStream = java.io.ByteArrayOutputStream()
    resized.compress(android.graphics.Bitmap.CompressFormat.JPEG, 70, outputStream)
    val base64 = android.util.Base64.encodeToString(outputStream.toByteArray(), android.util.Base64.NO_WRAP)
    return "data:image/jpeg;base64,$base64"
}

// encodeProfileImage()로 만든 데이터 URI(또는 순수 base64 문자열)를 다시 ImageBitmap으로 디코딩.
// 형식이 안 맞거나 비어있으면 null - 호출부에서 플레이스홀더 아이콘으로 대체함.
internal fun decodeProfileImage(data: String?): ImageBitmap? {
    if (data.isNullOrBlank()) return null
    return try {
        val base64Part = if (data.contains(",")) data.substringAfter(",") else data
        val bytes = android.util.Base64.decode(base64Part, android.util.Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
    } catch (e: Exception) {
        null
    }
}
