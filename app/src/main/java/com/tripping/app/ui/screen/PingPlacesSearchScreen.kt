// [파일 설명] Ping "코스 상세" 화면의 "+" 버튼 전용 장소 선택 화면. 지도에서 장소를 골라 그 코스에 새 핑을 등록할 때 씀 (일반 장소 검색인 PlaceSearchScreen과는 별개).
package com.tripping.app.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraPosition
import com.naver.maps.map.compose.ExperimentalNaverMapApi
import com.naver.maps.map.compose.Marker
import com.naver.maps.map.compose.MarkerState
import com.naver.maps.map.compose.NaverMap
import com.naver.maps.map.compose.rememberCameraPositionState
import com.naver.maps.map.overlay.OverlayImage
import com.tripping.app.R
import com.tripping.app.data.response.PlaceSearchResponse
import com.tripping.app.ui.component.PlaceInfoCard
import com.tripping.app.viewmodel.PlaceCategory
import com.tripping.app.viewmodel.PlaceSearchViewModel

private val regionOptions = listOf("전체", "서울", "강원", "부산", "제주")
private val BluePrimary = Color(0xFF4A72C4)

@OptIn(ExperimentalNaverMapApi::class)
@Composable
fun PingPlaceSearchScreen(
    viewModel: PlaceSearchViewModel = viewModel(),
    onPlaceSelected: (PlaceSearchResponse) -> Unit
) {
    val places by viewModel.places.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    var selectedPlace by remember { mutableStateOf<PlaceSearchResponse?>(null) }

    var regionMenuExpanded by remember { mutableStateOf(false) }
    var selectedRegion by remember { mutableStateOf("지역") }

    val pinIcon = remember { OverlayImage.fromResource(R.drawable.ic_map_pin) }

    LaunchedEffect(Unit) { viewModel.loadPlaces() }

    Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ===== 상단 바 =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(top = 20.dp, start = 15.dp, end = 15.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "핑을 추가할 장소를 선택해주세요",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF818181),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { regionMenuExpanded = true }
                        ) {
                            Text(
                                text = selectedRegion,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.Black
                            )
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = null, tint = Color.Black)
                        }
                        DropdownMenu(
                            expanded = regionMenuExpanded,
                            onDismissRequest = { regionMenuExpanded = false }
                        ) {
                            regionOptions.forEach { region ->
                                DropdownMenuItem(
                                    text = { Text(region) },
                                    onClick = {
                                        selectedRegion = region
                                        regionMenuExpanded = false
                                        // TODO: regionId 실제 코드로 변환해서 viewModel.onRegionSelected(...) 호출
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Row(
                        modifier = Modifier
                            .background(Color.White, RoundedCornerShape(8.dp))
                            .border(1.dp, Color(0xFFD9D9D9), RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PlaceCategory.entries.forEach { category ->
                            Text(
                                text = category.label,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedCategory == category) Color(0xFF22567E) else Color(0xFF818181),
                                modifier = Modifier.clickable { viewModel.onCategorySelected(category) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(58.dp, 38.dp)
                            .shadow(2.dp, RoundedCornerShape(24.dp))
                            .background(Color.White, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "검색", tint = Color(0xFF818181))
                    }
                }
            }

            // ===== 지도 =====
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition(LatLng(37.5665, 126.9780), 12.0)
                }

                NaverMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { _, _ -> selectedPlace = null }
                ) {
                    places.forEach { place ->
                        Marker(
                            state = MarkerState(position = LatLng(place.latitude, place.longitude)),
                            icon = pinIcon,
                            width = 34.dp,
                            height = 34.dp,
                            captionText = place.name,
                            onClick = {
                                selectedPlace = place
                                true
                            }
                        )
                    }
                }

                // 장소 선택 시: 정보 카드 + "이 장소로 핑 추가하기" 버튼
                selectedPlace?.let { place ->
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp)
                    ) {
                        PlaceInfoCard(place = place)

                        Spacer(modifier = Modifier.height(10.dp))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(BluePrimary)
                                .clickable { onPlaceSelected(place) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "이 장소로 핑 추가하기",
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}