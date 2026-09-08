package com.tripping.app.data.response

data class PlannedRoutePlaceResponse(
    val routePlaceId: Long,
    val spotId: Long,
    val spotName: String,
    val visitOrder: Int
)

data class PlannedRouteResponse(
    val routeId: Long,
    val title: String,
    val isShared: Boolean?,
    val places: List<PlannedRoutePlaceResponse>
)