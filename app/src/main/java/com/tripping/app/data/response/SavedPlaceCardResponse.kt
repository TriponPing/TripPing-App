package com.tripping.app.data.response

// 마이페이지 "저장한 장소" 탭용 - GET /users/me/places/saved
data class SavedPlaceCardResponse(
    val spotId: Long,
    val name: String?,
    val category: String?,
    val imageUrl: String?,
    val pingCount: Long,
    val savedCount: Long
)
