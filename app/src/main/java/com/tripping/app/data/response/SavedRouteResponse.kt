package com.tripping.app.data.response

// GET /users/me/routes/saved 응답의 리스트 항목
data class SavedRouteResponse(
    val savedRouteId: Long,
    val tripId: Long, // = 원본 actualRouteId, DELETE /routes/{routeId}/saved 호출할 때 이 값 사용
    val travelDate: String,
    val memberCount: Int?,
    val representativeSpotName: String?,
    val representativeImageUrl: String?,
    val placeCount: Int?, // 이 루트에 포함된 방문 장소(스팟) 개수 - "핑 N개" 표시용
    val savedAt: String
)
