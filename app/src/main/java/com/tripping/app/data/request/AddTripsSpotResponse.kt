// [파일 설명] 완료된 여행에 방문 스팟 추가 응답 (POST /trips/{routeId}/spots)
package com.tripping.app.data.response

data class AddTripSpotResponse(
    val visitOrder: Int?,
    val spotId: Long?,
    val spotName: String?,
    val category: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val visitTime: String?
)