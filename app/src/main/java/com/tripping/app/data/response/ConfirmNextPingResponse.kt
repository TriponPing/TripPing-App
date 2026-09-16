// [파일 설명] "다음 핑 찍기" 응답. POST /routes/{routeId}/pings/next
package com.tripping.app.data.response

data class ConfirmNextPingResponse(
    val actualRouteSpotId: Long,
    val visitOrder: Int,
    val spotId: Long,
    val spotName: String?,
    val visitTime: String?,
    val hasNext: Boolean,
    val nextSpotName: String?
)
