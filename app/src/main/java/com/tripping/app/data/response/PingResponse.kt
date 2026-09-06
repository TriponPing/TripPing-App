package com.tripping.app.data.response

// 1. 최상단 응답 모델 (Swagger 예시 기준)
data class PingResponse(
    val routeId: Long,
    val status: String,
    val travelDate: String,
    val pings: List<PingDto>
)

// 2. 핑 아이템 모델 (Swagger 예시 기준)
data class PingDto(
    val pingId: Long,
    val spotId: Long,
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val pingTime: String,
    val isConfirmed: Boolean
)