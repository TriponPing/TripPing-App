package com.tripping.app.data.request

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
    val startTime: String?,   // "HH:mm:ss" 형식 문자열
    val endTime: String?,     // "HH:mm:ss" 형식 문자열
    val mealIncluded: Boolean? = null,
    val maxSpotCount: Int? = null,
    val walkTimeLimit: Int? = null,
    val mustVisitSpotIds: List<Long>? = null,
    val excludeSpotIds: List<Long>? = null
)