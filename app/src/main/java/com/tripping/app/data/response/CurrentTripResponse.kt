package com.tripping.app.data.response

data class CurrentTripResponse(
    val actualRouteId: Long,
    val userId: Long,
    val travelDate: String?,
    val companionType: String?,
    val transport: String?,
    val memberCount: Int?,
    val status: String?
)