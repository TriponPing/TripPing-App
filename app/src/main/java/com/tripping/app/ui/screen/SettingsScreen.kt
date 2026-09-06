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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
// 단, 뱃지 종류 자체(카탈로그)는 실제 기획이 아직 없어서 지금은 비어있음(BadgeCatalog 참고) -
// 조회/저장/꺼내기 기능만 먼저 만들어둔 상태고, 실제 뱃지가 추가되면 그대로 여기 뜸.

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

    // 프로필 사진 - 갤러리에서 고른 사진 로컬 미리보기 (TODO: 백엔드에 프로필 사진 업로드 API 생기면 여기서 실제 업로드 연결)
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
                withContext(Dispatchers.Main) {
                    pickedProfileImage = bitmap?.asImageBitmap()
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
                        val picked = pickedProfileImage
                        if (picked != null) {
                            Image(
                                bitmap = picked,
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

            // ===== 뱃지 - 늘어날 때마다 이 공간이 알아서 줄바꿈하면서 정렬됨 =====
            // 체크 표시된 뱃지 = 아래 "꺼낼 뱃지"에 노출 중인 뱃지. 눌러서 켜고 끌 수 있음.
            Text(text = "뱃지", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            if (badges.isEmpty()) {
                Text(text = "아직 획득한 뱃지가 없어요", fontSize = 12.sp, color = ColorTextSecondary)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    badges.forEach { badge ->
                        BadgeItem(
                            badge = badge,
                            onClick = { viewModel.toggleFeaturedBadge(badge.code) }
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
                    .background(ColorBadgeCircleBg),
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
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = badge.label,
            fontSize = 10.sp,
            color = ColorTextSecondary,
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
