package com.tripping.app.data.response

// GET /users/me/badges, PUT /users/me/badges/featured 응답 항목
data class BadgeResponse(
    val code: String,
    val label: String,
    val emoji: String,
    val featured: Boolean // true면 "꺼낼 뱃지"(마이페이지 프로필에 노출)에 포함됨
)
