// [파일 설명] 핑 등록 API(POST /routes/{routeId}/pings) 요청 바디. 방문 장소(spotId/이름/위경도)를 서버에 보낼 때 씀.
package com.tripping.app.data.request

data class CreatePingRequest(
    val spotId: Long,
    val placeName: String,
    val latitude: Double,
    val longitude: Double
)