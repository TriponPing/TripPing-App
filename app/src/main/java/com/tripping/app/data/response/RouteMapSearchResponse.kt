package com.tripping.app.data.response

data class RouteMapSearchResponse(
    val routeId: Long,
    val places: List<RouteMapPlaceResponse>
)