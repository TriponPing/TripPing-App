package com.tripping.app.data.response

// GET /trips/{routeId} 응답 - 탐색 화면에서 루트 하나를 상세조회할 때 씀
// (마이페이지의 TripDetailResponse랑은 다른 용도라 이름 다르게 지음: 저건 "내가 다녀온 여행 기록",
//  이건 "탐색에서 보는 남의 공개 루트 상세")
data class PopularTripDetailResponse(
    val routeId: Long,
    val writerNickname: String,
    val writerProfileImage: String?,
    val writerLevel: String?,
    val stops: List<StopSummaryResponse>,
    val placeCount: Int,
    val photoUrl: String?,
    val savedCount: Long,
    val coordinates: List<CoordinateResponse>
)

data class StopSummaryResponse(
    val spotId: Long,
    val name: String,
    val averageRating: Double?,
    val registeredRouteCount: Long
)