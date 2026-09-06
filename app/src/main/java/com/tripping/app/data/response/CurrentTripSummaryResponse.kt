package com.tripping.app.data.response

// GET /trips/current-summary - 백엔드 home.dto.response.CurrentTripSummaryResponse 1:1 대응
// (Ping 도메인 코드는 안 건드리고, 홈 화면 전용으로 새로 추가된 백엔드 API)
data class CurrentTripSummaryResponse(
    val actualRouteId: Long,
    val companionType: String?,
    val transport: String?,
    val memberCount: Int?,
    val travelDate: String?,
    val status: String?,
    val pingCount: Long,
    val visitedPlaceNames: List<String>
)
