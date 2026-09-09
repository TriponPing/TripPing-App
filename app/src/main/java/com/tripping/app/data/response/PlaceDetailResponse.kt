package com.tripping.app.data.response

// GET /places/{placeId}/detail 응답
// (마이페이지 TripDetailResponse 안에 있는 SpotDetail이랑 이름 안 겹치게 PlaceDetailResponse로 지음)
data class PlaceDetailResponse(
    val spotId: Long,
    val name: String,
    val address: String?,
    val category: String,
    val imageUrl: String?,
    val description: String?,
    val pingCount: Long,
    val popularTimeSlot: String?,
    val registeredRoutes: List<RegisteredRouteCardResponse>,
    val reviews: List<PlaceReviewResponse>
)

data class RegisteredRouteCardResponse(
    val actualRouteId: Long,
    val themeName: String,
    val photoUrl: String?,
    val placeCount: Int
)

data class PlaceReviewResponse(
    val writerNickname: String,
    val rating: Int?,
    val reviewComment: String?
)