package com.tripping.app.data.response

// GET /users/me/trips/{tripId} 응답 - 여행 기록 상세(방문 스팟, 사진, 후기 포함)
data class TripDetailResponse(
    val tripId: Long,
    val travelDate: String,
    val companionType: String?,
    val transport: String?,
    val memberCount: Int?,
    val status: String?,
    val isPublic: Boolean?,
    val spots: List<SpotDetail>
) {
    data class SpotDetail(
        val visitOrder: Int?,
        val spotId: Long?,
        val spotName: String?,
        val category: String?,
        val address: String?,
        val latitude: Double?,
        val longitude: Double?,
        val visitTime: String?,
        val rating: Int?,
        val photoUrl: String?,
        val reviewComment: String?
    )
}
