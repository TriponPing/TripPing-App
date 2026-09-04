package com.tripping.app.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.runtime.remember
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.composed

enum class BottomNavTab { HOME, SEARCH, ROUTE, PING, MY }

@Composable
fun BottomNavBar(
    selectedTab: BottomNavTab,
    onTabSelected: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavItem("홈", Icons.Filled.Home, selectedTab == BottomNavTab.HOME) {
            onTabSelected(BottomNavTab.HOME)
        }
        NavItem("탐색", Icons.Filled.Search, selectedTab == BottomNavTab.SEARCH) {
            onTabSelected(BottomNavTab.SEARCH)
        }
        // 가운데 루트 생성 버튼 (조금 크게)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickableNoRipple { onTabSelected(BottomNavTab.ROUTE) }
        ) {
            Icon(
                imageVector = Icons.Filled.AddCircle,
                contentDescription = "루트 생성",
                tint = Color.Black,
                modifier = Modifier.size(40.dp)
            )
            Text("루트", fontSize = 14.sp, color = Color.Black)
        }
        NavItem("Ping", Icons.Filled.LocationOn, selectedTab == BottomNavTab.PING) {
            onTabSelected(BottomNavTab.PING)
        }
        NavItem("마이", Icons.Filled.Person, selectedTab == BottomNavTab.MY) {
            onTabSelected(BottomNavTab.MY)
        }
    }
}

@Composable
private fun NavItem(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val color = if (selected) Color(0xFF22567E) else Color.Black
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickableNoRipple(onClick)
            .padding(6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Text(label, fontSize = 14.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// 클릭 시 물결 효과(ripple) 없이 심플하게 처리하기 위한 확장함수
// 클릭 시 물결 효과(ripple) 없이 심플하게 처리하기 위한 확장함수
private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        indication = null,
        interactionSource = remember { MutableInteractionSource() },
        onClick = onClick
    )
}