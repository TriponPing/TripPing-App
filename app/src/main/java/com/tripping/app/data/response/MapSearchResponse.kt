package com.tripping.app.data.response

// GET /users/me/map/search?keyword= 응답 항목
data class MapSearchResponse(
    val tripId: Long,
    val type: String, // "DRAWN" | "SAVED"
    val travelDate: String
)
