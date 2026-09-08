package com.tripping.app.data.response

// 백엔드 TripResponse.java 미러링
data class TripResponse(
    val actualRouteId: Long,
    val travelDate: String?,
    val companionType: String?,
    val transport: String?,
    val memberCount: Int?,
    val status: String?
)