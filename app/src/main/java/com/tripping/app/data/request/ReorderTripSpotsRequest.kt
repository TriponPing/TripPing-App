// [파일 설명] Ping "기록" 탭에서 꾹 눌러 드래그로 바꾼 새 순서를 저장하는 요청. PATCH /trips/{routeId}/spots/order
package com.tripping.app.data.request

data class ReorderTripSpotsRequest(
    val actualRouteSpotIds: List<Long>
)
