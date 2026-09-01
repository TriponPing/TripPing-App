package com.tripping.app.data.response

data class PlaceSearchResponse(
    val spotId: Long,
    val name: String,
    val category: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val imageUrl: String?,
    val description: String?
)