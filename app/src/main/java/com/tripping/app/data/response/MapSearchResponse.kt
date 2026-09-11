package com.tripping.app.data.response

// GET /users/me/map/search?keyword= 응답 항목
data class MapSearchResponse(
    val tripId: Long,
    val type: String, // "DRAWN" | "SAVED"
    val travelDate: String,
    val spotName: String?, // 👈 새로 추가: 대표 스팟 이름 - 검색 결과 목록에 표시할 이름
    val latitude: Double?, // 👈 새로 추가: 눌렀을 때 바로 지도 이동시키기 위한 좌표
    val longitude: Double?
)
