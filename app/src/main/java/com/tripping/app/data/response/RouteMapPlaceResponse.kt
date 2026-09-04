package com.tripping.app.data.response

data class RouteMapPlaceResponse(
    val routePlaceId: Long,
    val spotId: Long,
    val spotName: String,
    val latitude: Double,
    val longitude: Double,
    val visitOrder: Int
)