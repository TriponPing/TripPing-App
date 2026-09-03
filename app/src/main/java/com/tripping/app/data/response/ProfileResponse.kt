package com.tripping.app.data.response

// GET /users/me, PATCH /users/me 응답
data class ProfileResponse(
    val userId: Long,
    val email: String,
    val nickname: String,
    val profileImage: String?,
    val regionId: String?,
    val language: String?,
    val level: String?,
    val updatedAt: String?
)
