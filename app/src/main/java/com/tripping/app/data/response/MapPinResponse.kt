package com.tripping.app.data.response

// GET /users/me/map 응답의 리스트 항목 (지도에 찍을 핀 1개)
data class MapPinResponse(
    val tripId: Long,
    val type: String, // "DRAWN" | "SAVED"
    val travelDate: String,
    val latitude: Double?,
    val longitude: Double?,
    val representativeSpotName: String?
)
