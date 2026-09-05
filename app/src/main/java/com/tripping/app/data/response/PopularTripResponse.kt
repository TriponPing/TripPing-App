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
    val photoUrl: String?
)
