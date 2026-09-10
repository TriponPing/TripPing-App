// [파일 설명] Ping 탭 > "로그" 화면 UI. 지역별로 다른 사람들이 공개해둔 루트(로그 커뮤니티)를 보여줌.
package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tripping.app.data.response.RouteSummaryResponse
import com.tripping.app.viewmodel.CommunityViewModel

// ===== 색상 (필요시 공통 파일로 관리) =====
private val GrayBg = Color(0xFFF3F3F5)
private val GrayText = Color(0xFF9A9A9A)
private val CardBorder = Color(0xFFECECEC)

@Composable
internal fun PingLogContent(
    onCourseClick: (Long) -> Unit,
    viewModel: CommunityViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.loadInitialRegions()
    }

    val regions = viewModel.regions
    val selectedRegion = viewModel.selectedRegion
    val routes = viewModel.routes

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RegionDropdown(
                    regions = regions,
                    selectedRegion = selectedRegion,
                    onRegionSelected = { viewModel.selectRegion(it) }
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "지역별 인기순위를 만나보세요", fontSize = 12.sp, color = GrayText)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (routes.isEmpty()) {
            item {
                Text(
                    text = if (viewModel.isLoadingRoutes) "불러오는 중..." else "이 지역에 등록된 루트가 아직 없어요",
                    fontSize = 13.sp,
                    color = GrayText,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            }
        } else {
            items(routes) { route ->
                RouteCourseCard(route = route, onClick = { onCourseClick(route.routeId) })
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

@Composable
private fun RegionDropdown(
    regions: List<com.tripping.app.data.response.RegionResponse>,
    selectedRegion: com.tripping.app.data.response.RegionResponse?,
    onRegionSelected: (com.tripping.app.data.response.RegionResponse) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier.clickable { expanded = true },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = selectedRegion?.regionName ?: "지역", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "⌄", fontSize = 18.sp, color = Color.Black)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            regions.forEach { region ->
                DropdownMenuItem(
                    text = { Text(region.regionName) },
                    onClick = {
                        onRegionSelected(region)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun RouteCourseCard(route: RouteSummaryResponse, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(GrayBg)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = route.writerNickname, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(verticalAlignment = Alignment.Top) {
            // TODO: 실제 경로 미리보기 이미지/지도로 교체
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFE8EEF5))
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${route.travelDate ?: "날짜 미정"} 루트",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(text = "🔖", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = route.savedCount.toString(), fontSize = 12.sp, color = GrayText)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = route.spotNames.ifEmpty { listOf("경유지 정보 없음") }.joinToString(" → "),
                    fontSize = 13.sp,
                    color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Ping 개수 : ${route.spotCount}개",
                    fontSize = 11.sp,
                    color = GrayText,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.End
                )
            }
        }
    }
}
