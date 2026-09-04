package com.tripping.app.ui.component

import android.graphics.PointF
import android.os.Bundle
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.tripping.app.R
import kotlin.math.abs

/**
 * 네이버 지도(MapView)를 Compose에서 쓰기 위한 래퍼.
 * MapView는 전통적인 XML View라서 AndroidView로 감싸고,
 * onCreate/onStart/onResume/onPause/onStop/onDestroy를 화면 생명주기에 맞춰 직접 호출해줘야 정상 동작함.
 *
 * onMapReady는 지도가 준비됐을 때 딱 한 번 NaverMap 인스턴스를 넘겨줌.
 * 마커/경로선 등은 이 콜백으로 받은 NaverMap을 remember한 뒤 별도 LaunchedEffect에서 갱신하는 걸 권장
 * (여기서 매번 다시 그리면 recompose마다 중복으로 그려짐).
 */
@Composable
fun NaverMapContainer(
    modifier: Modifier = Modifier,
    onMapReady: (NaverMap) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val currentOnMapReady by rememberUpdatedState(onMapReady)

    val mapView = remember {
        MapView(context).apply { id = View.generateViewId() }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val bundle = Bundle()
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_CREATE -> mapView.onCreate(bundle)
                Lifecycle.Event.ON_START -> mapView.onStart()
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                Lifecycle.Event.ON_STOP -> mapView.onStop()
                Lifecycle.Event.ON_DESTROY -> mapView.onDestroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView.also {
                it.getMapAsync { naverMap -> currentOnMapReady(naverMap) }
            }
        }
    )
}

// 지점 목록이 다 보이게 카메라를 맞춰주는 CameraUpdate를 만들어줌.
// 지점들이 전부 같은(또는 거의 같은) 좌표면 LatLngBounds가 사실상 점 하나가 돼서
// fitBounds가 극단적으로(건물 하나 크기까지) 확대해버리는 문제가 있어서, 그럴 땐 고정 줌으로 대체함.
private const val DEGENERATE_BOUNDS_THRESHOLD = 0.0005 // 대략 50m

fun cameraUpdateToShowAll(
    points: List<LatLng>,
    paddingPx: Int = 200,
    fallbackZoom: Double = 15.0
): CameraUpdate? {
    if (points.isEmpty()) return null
    if (points.size == 1) {
        return CameraUpdate.scrollAndZoomTo(points.first(), fallbackZoom)
    }
    val bounds = LatLngBounds.fromOrNull(points) ?: return null
    val latSpan = abs(bounds.northEast.latitude - bounds.southWest.latitude)
    val lngSpan = abs(bounds.northEast.longitude - bounds.southWest.longitude)
    return if (latSpan < DEGENERATE_BOUNDS_THRESHOLD && lngSpan < DEGENERATE_BOUNDS_THRESHOLD) {
        CameraUpdate.scrollAndZoomTo(bounds.center, fallbackZoom)
    } else {
        CameraUpdate.fitBounds(bounds, paddingPx)
    }
}

// 지도에 찍는 핑 마커 아이콘 - 앱 전체에서 이거 하나로 통일해서 씀 (Ping 화면이랑 같은 blueping.png).
// 기본 눈물방울 모양 핀 대신 이 원형 아이콘을 쓰고, anchor를 가운데로 맞춰서 좌표 위치에 원 중심이 오게 함.
private val PingMarkerIcon: OverlayImage by lazy { OverlayImage.fromResource(R.drawable.blueping) }

fun Marker.applyPingIcon() {
    icon = PingMarkerIcon
    anchor = PointF(0.5f, 0.5f)
}
