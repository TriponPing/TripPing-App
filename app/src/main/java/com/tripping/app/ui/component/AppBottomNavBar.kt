package com.tripping.app.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripping.app.R

// ===== 앱 공통 하단 네비게이션 바 =====
// 홈/탐색/루트/Ping/마이 화면 어디서든 재사용.
// TODO: 화면마다 현재 탭이 뭔지 알려주는 파라미터(selectedTab) 추가해서
//       각 화면이 자기 탭을 selected = true로 넘기도록 바꾸면 더 정확해짐.
//       지금은 마이페이지에서만 쓰이고 있어서 "마이"만 선택 상태로 고정해둠.

private val ColorAccentBlue = Color(0xFF0074CE)

@Composable
private fun NavIcon(resId: Int, contentDescription: String) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = Modifier.size(24.dp)
    )
}

@Composable
fun AppBottomNavBar(
    selectedTab: AppBottomNavTab = AppBottomNavTab.MY,
    onHomeClick: () -> Unit = {},
    onSearchClick: () -> Unit = {},
    onRouteClick: () -> Unit = {},
    onPingClick: () -> Unit = {},
    onMyClick: () -> Unit = {}
) {
    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
        containerColor = Color.White
    ) {
        NavigationBarItem(
            selected = selectedTab == AppBottomNavTab.HOME,
            onClick = onHomeClick,
            icon = { NavIcon(R.drawable.icon_home, "홈") },
            label = { Text("홈", fontSize = 11.sp) },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = selectedTab == AppBottomNavTab.SEARCH,
            onClick = onSearchClick,
            icon = { NavIcon(R.drawable.icon_search, "탐색") },
            label = { Text("탐색", fontSize = 11.sp) },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = selectedTab == AppBottomNavTab.ROUTE,
            onClick = onRouteClick,
            icon = { NavIcon(R.drawable.plus_circle, "루트") },
            label = { Text("루트", fontSize = 11.sp) },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = selectedTab == AppBottomNavTab.PING,
            onClick = onPingClick,
            icon = { NavIcon(R.drawable.icon_map_pin, "Ping") },
            label = { Text("Ping", fontSize = 11.sp) },
            colors = navItemColors()
        )
        NavigationBarItem(
            selected = selectedTab == AppBottomNavTab.MY,
            onClick = onMyClick,
            icon = { NavIcon(R.drawable.icon_user, "마이") },
            label = { Text("마이", fontSize = 11.sp) },
            colors = navItemColors()
        )
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = ColorAccentBlue,
    selectedTextColor = ColorAccentBlue
)

enum class AppBottomNavTab {
    HOME, SEARCH, ROUTE, PING, MY
}
