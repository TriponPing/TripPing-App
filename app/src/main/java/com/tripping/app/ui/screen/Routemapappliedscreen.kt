package com.tripping.app.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import com.tripping.app.R

private val AccentBlue = Color(0xFF0074CE)
private val AccentPurple = Color(0xFF7B5AF7)
private val GrayText = Color(0xFF818181)
private val LightGrayBorder = Color(0xFFE0E0E0)

/**
 * "적용" 버튼을 눌렀을 때 보여주는 화면.
 * 선택된 추천 루트(route)의 장소들을 네이버 지도 위에 마커 + 경로선으로 표시하고,
 * 하단에서 "등록"(저장) 또는 "여행 바로 시작하기"를 선택할 수 있어요.
 *
 * 주의: RoutePlaceItem.latitude / longitude 가 null인 장소는 지도에 표시되지 않아요.
 *
 * @param onRegisterClick "등록" — 이 루트를 저장만 하고 나중에 시작
 * @param onStartTripClick "여행 바로 시작하기" — 저장과 동시에 바로 여행 시작
 */
@Composable
fun RouteMapAppliedScreen(
    route: RecommendedRoute,
    onBackClick: () -> Unit = {},
    onRegisterClick: () -> Unit = {},
    onStartTripClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "루트",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 12.dp)
        )
        BackButtonRow(onClick = onBackClick)
        HorizontalDivider(color = LightGrayBorder, thickness = 1.dp)

        RouteStepIndicator(currentStep = 3)

        Box(modifier = Modifier.weight(1f)) {
            NaverRouteMapView(
                modifier = Modifier.fillMaxSize(),
                places = route.places
            )
        }

        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Button(
                onClick = onRegisterClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text(text = "등록", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onStartTripClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
            ) {
                Text(text = "여행 바로 시작하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RouteStepIndicator(currentStep: Int) {
    val steps = listOf("정보 입력", "추천 확인", "완료")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val stepNumber = index + 1
            val isActive = stepNumber == currentStep
            val isDone = stepNumber < currentStep

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isActive || isDone) AccentBlue else Color(0xFFE0E0E0),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = stepNumber.toString(), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    color = if (isActive) AccentBlue else GrayText
                )
            }
            if (index < steps.size - 1) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f).padding(bottom = 20.dp),
                    color = if (stepNumber < currentStep) AccentBlue else Color(0xFFE0E0E0),
                    thickness = 2.dp
                )
            }
        }
    }
}

@Composable
private fun NaverRouteMapView(
    modifier: Modifier = Modifier,
    places: List<RoutePlaceItem>
) {
    val context = LocalContext.current
    val mapView = remember { MapView(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(null)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    AndroidView(
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            view.getMapAsync { naverMap ->
                drawRoute(naverMap, places)
            }
        }
    )
}

private fun drawRoute(naverMap: NaverMap, places: List<RoutePlaceItem>) {
    val validPlaces = places
        .sortedBy { it.order }
        .filter { it.latitude != null && it.longitude != null }

    if (validPlaces.isEmpty()) return

    val latLngs = validPlaces.map { LatLng(it.latitude!!, it.longitude!!) }
    val pinIcon = OverlayImage.fromResource(R.drawable.blueping)

    latLngs.forEach { latLng ->
        Marker().apply {
            position = latLng
            icon = pinIcon
            map = naverMap
        }
    }

    // 경로선 색을 마커(blueping) 색과 통일
    val pinLineColor = 0xFF0074CE.toInt()

    PathOverlay().apply {
        coords = latLngs
        color = pinLineColor
        outlineColor = pinLineColor
        map = naverMap
    }

    naverMap.moveCamera(CameraUpdate.scrollTo(latLngs.first()))
}

@Composable
private fun BackButtonRow(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowLeft,
            contentDescription = "뒤로가기",
            tint = Color.Black,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(2.dp))
        Text(text = "뒤로가기", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
    }
}