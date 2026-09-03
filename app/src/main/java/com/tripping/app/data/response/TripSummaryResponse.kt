package com.tripping.app.data.response

// GET /users/me/trips/recent, GET /users/me/trips 응답의 리스트 항목
// 참고: title/핑개수/소요시간 필드는 백엔드에 없음 - representativeSpotName으로 대체
data class TripSummaryResponse(
    val tripId: Long,
    val travelDate: String,
    val status: String,
    val memberCount: Int?,
    val representativeSpotName: String?,
    val representativeImageUrl: String?
)
