package com.tripping.app.data.response

// GET /users/me/map/detail?type=drawn|saved 응답 - 타입별 전체 경로(방문 순서대로) 1건
data class MapDetailResponse(
    val tripId: Long,
    val travelDate: String,
    val type: String, // "DRAWN" | "SAVED" (요청한 type과 동일)
    val spots: List<SpotPoint>
) {
    data class SpotPoint(
        val visitOrder: Int?,
        val spotId: Long?,
        val spotName: String?,
        val latitude: Double?,
        val longitude: Double?,
        val visitTime: String?
    )
}
