package com.tripping.app.ui.component

import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
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
import java.util.Locale
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

    // 👈 수정: 검색창처럼 지도 위에 떠있는 Compose 텍스트 필드가 있는 화면(나의 여행 지도 등)에서
    // 한글이 아예 입력이 안 되던 버그. 지도(MapView)와 그 내부 뷰들이 기본적으로 포커스를
    // 받을 수 있는 상태라서, IME가 한글 자모를 조합하는 도중에 시스템이 포커스를 지도 쪽으로
    // 가져가버려 조합 중이던 글자가 그대로 날아갔던 것으로 보임. 영어/숫자는 키 하나에 바로
    // 글자가 확정돼서 문제가 안 보였을 뿐. 지도는 키보드 포커스가 필요 없으니 아예 막아버림.
    val mapView = remember {
        MapView(context).apply {
            id = View.generateViewId()
            isFocusable = false
            isFocusableInTouchMode = false
            descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        }
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
                it.getMapAsync { naverMap ->
                    // 👈 수정: locale 지정 안 하면 기기 시스템 언어를 따라가서, 에뮬레이터/기기 언어가
                    // 영어면 지도 위 장소 이름(POI)도 영어로 나옴 - 한국어로 고정.
                    naverMap.locale = Locale.KOREA
                    currentOnMapReady(naverMap)
                }
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
