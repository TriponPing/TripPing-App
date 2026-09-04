package com.tripping.app.data.response

// GET /users/me/trips/recent, GET /users/me/trips 응답의 리스트 항목
// 참고: title/소요시간 필드는 백엔드에 없음 - representativeSpotName으로 대체
data class TripSummaryResponse(
    val tripId: Long,
    val travelDate: String,
    val status: String,
    val memberCount: Int?,
    val representativeSpotName: String?,
    val representativeImageUrl: String?,
    val placeCount: Int? // 이 여행에 포함된 방문 장소 개수 - 카드의 "핑 N개" 표시에 사용
)
