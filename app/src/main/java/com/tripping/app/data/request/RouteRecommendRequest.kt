package com.tripping.app.data.request

data class RouteTimeRequest(
    val hour: Int,
    val minute: Int,
    val second: Int = 0,
    val nano: Int = 0
)

data class RouteRecommendRequest(
    val regionId: String?,
    val memberCount: Int? = null,
    val startDate: String,
    val endDate: String,
    val companionType: String?,
    val ageGroup: String? = null,
    val transport: String?,
    val totalTime: Int? = null,
    val startPlace: String?,
    val endPlace: String? = null,
    val startTime: RouteTimeRequest?,
    val endTime: RouteTimeRequest?,
    val mealIncluded: Boolean? = null,
    val maxSpotCount: Int? = null,
    val walkTimeLimit: Int? = null,
    val mustVisitSpotIds: List<Long>? = null,
    val excludeSpotIds: List<Long>? = null
)