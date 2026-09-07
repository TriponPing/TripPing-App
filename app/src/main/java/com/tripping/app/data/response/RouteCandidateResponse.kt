package com.tripping.app.data.response

data class RouteCandidateSpotResponse(
    val spotId: Long,
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val visitOrder: Int
)

data class RouteCandidateResponse(
    val candidateOrder: Int,
    val theme: String,
    val totalDistanceKm: Double,
    val totalWalkTimeMinutes: Int,
    val spots: List<RouteCandidateSpotResponse>
)