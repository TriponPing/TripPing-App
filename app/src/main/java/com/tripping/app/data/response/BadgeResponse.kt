package com.tripping.app.data.response

// GET /users/me/badges, PUT /users/me/badges/featured 응답 항목
data class BadgeResponse(
    val code: String,
    val label: String,
    val emoji: String,
    val conditionDesc: String, // 달성 조건 설명 (예: "여행 1개를 완주해보세요") - 안 딴 뱃지에 잠금 상태로 노출
    val earned: Boolean,       // true면 실제로 달성한 뱃지
    val featured: Boolean      // true면 "꺼낼 뱃지"(마이페이지 프로필에 노출)에 포함됨
)
