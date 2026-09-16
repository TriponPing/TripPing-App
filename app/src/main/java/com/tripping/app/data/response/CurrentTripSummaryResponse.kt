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
    val visitedPlaceNames: List<String>,
    // 👈 새로 추가: visitedPlaceNames 중 앞에서부터 몇 개가 실제로 찍힌(confirm된) 핑인지.
    // 홈 위젯에서 이미 찍은 핑(파란색)/다음 찍을 핑(포커스)을 구분해서 그리는 데 씀.
    val confirmedCount: Long = 0,
    // 👈 새로 추가: 가장 최근에 찍힌 핑의 좌표 - "내 주변 코스"를 이 좌표 기준으로 보여주기 위함.
    val lastPingLatitude: Double? = null,
    val lastPingLongitude: Double? = null
)
