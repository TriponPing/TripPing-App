// [파일 설명] 완료된 여행에 놓친 방문 스팟을 나중에 추가하는 요청 (POST /trips/{routeId}/spots)
package com.tripping.app.data.request

data class AddTripSpotRequest(
    val spotId: Long,
    val latitude: Double,
    val longitude: Double
)