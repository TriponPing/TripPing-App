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
    val tags: List<String> = emptyList(), // 키워드로 루트 조회할 때만 채워짐, 그 외엔 빈 목록
    // 👈 새로 추가: 방문 장소 카테고리로 즉석 계산한 테마 이름 (예: "카페투어 루트")
    val title: String? = null,
    // 👈 새로 추가: "내 주변 코스"(GET /trips/nearby) 응답에서만 채워짐. 그 외엔 null
    val distanceKm: Double? = null
)

data class RouteCoordinate(
    val latitude: Double,
    val longitude: Double
)
