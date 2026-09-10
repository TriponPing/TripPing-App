package com.tripping.app.data.response

// POST /region-pings 응답
data class RegionPingResponse(
    val regionPingId: Long,
    val spotId: Long,
    val spotName: String?,
    val address: String?,
    val latitude: Double?,
    val longitude: Double?,
    val regionId: String?,
    val rating: Int,
    val reviewComment: String?,
    val writerNickname: String?,
    val createdAt: String?
)
