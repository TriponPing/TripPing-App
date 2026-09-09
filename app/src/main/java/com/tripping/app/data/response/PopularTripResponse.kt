package com.tripping.app.data.response

data class PopularTripResponse(
    val routeId: Long,
    val writerNickname: String?,
    val travelDate: String?,
    val companionType: String?,
    val transport: String?,
    val memberCount: Int?,
    val status: String?,
    val savedCount: Long,
    val stopNames: List<String>,
    val photoUrl: String?,
    val pingCount: Long,
    val coordinates: List<RouteCoordinate>,
    val tags: List<String> = emptyList() // 키워드로 루트 조회할 때만 채워짐, 그 외엔 빈 목록
)

data class RouteCoordinate(
    val latitude: Double,
    val longitude: Double
)
