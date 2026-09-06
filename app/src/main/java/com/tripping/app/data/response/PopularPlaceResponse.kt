package com.tripping.app.data.response

data class PopularPlaceResponse(
    val spotId: Long,
    val name: String,
    val category: String?,
    val savedCount: Long
)
