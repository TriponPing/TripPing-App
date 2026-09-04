package com.tripping.app.ui.component

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
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap

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
