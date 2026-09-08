package com.tripping.app.data.request

data class PlannedRouteCreateRequest(
    val title: String,
    val candidateId: Long? = null
)