package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.viewmodel.MyPageViewModel

// ===== 마이페이지 설정 화면 (마이페이지 오른쪽 위 톱니바퀴) =====
// 프로필(닉네임/레벨)은 실제 API(GET /users/me) 연동됨.
// 뱃지는 백엔드에 아직 관련 API가 없어서, 마이페이지 프로필 영역과 동일하게 임시 고정값을 씀.
// TODO: 뱃지 API 생기면 badges 목록/꺼낼 뱃지 선택 상태를 서버에서 받아오도록 교체.

private val ColorBackground = Color(0xFFF8F8FC)
private val ColorLevelChipBg = Color(0xFFD9D9D9)
private val ColorBadgeCircleBg = Color(0xFFEFF3F8)
private val ColorFeaturedTrayBg = Color(0xFFE3E3E3)

private data class ProfileBadge(val id: String, val label: String, val emoji: String)

// 지금 실제로 갖고 있는 뱃지는 이 2개뿐(마이페이지 프로필 영역이랑 동일) - 늘어나면 그냥 이 목록에 추가하면
// 아래 FlowRow가 알아서 줄바꿈하면서 정렬해줌.
private val earnedBadges = listOf(
    ProfileBadge("author", "추가 작성자", "📷"),
    ProfileBadge("attendance", "연속 출석", "📅")
)

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit = {},
    onLogoutConfirmed: () -> Unit = {},
    viewModel: MyPageViewModel = viewModel()
) {
    LaunchedEffect(Unit) { viewModel.loadProfile() }
    val profile by viewModel.profile.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }

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
                    // 아바타 (TODO: 실제 프로필 이미지로 교체 - GET /users/me의 profileImage)
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8EEF5)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "👤", fontSize = 40.sp)
                    }
                    // 프로필 사진 수정 - 텍스트 대신 사진 오른쪽 아래에 연필 아이콘으로
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable { /* TODO: 프로필 사진 수정 */ },
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
                            .background(ColorLevelChipBg)
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = profile?.level?.let { "Lv.$it" } ?: "-",
                            fontSize = 13.sp,
                            color = ColorTextPrimary
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
                        modifier = Modifier.clickable { /* TODO: 닉네임 수정 */ }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== 뱃지 - 늘어날 때마다 이 공간이 알아서 줄바꿈하면서 정렬됨 =====
            Text(text = "뱃지", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                earnedBadges.forEach { badge -> BadgeItem(badge) }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ===== 꺼낼 뱃지 - 마이페이지 프로필 영역에 실제로 보여줄 뱃지 (지금은 전부 다 보여주는 중) =====
            Text(text = "꺼낼 뱃지", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ColorFeaturedTrayBg)
                    .padding(16.dp)
            ) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    earnedBadges.forEach { badge -> BadgeItem(badge) }
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
}

@Composable
private fun BadgeItem(badge: ProfileBadge) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(ColorBadgeCircleBg),
            contentAlignment = Alignment.Center
        ) {
            Text(text = badge.emoji, fontSize = 20.sp)
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
