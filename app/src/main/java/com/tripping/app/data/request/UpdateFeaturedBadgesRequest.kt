package com.tripping.app.data.request

// PUT /users/me/badges/featured 요청 - 프로필에 노출하고 싶은 뱃지 code 전체 목록(교체)
data class UpdateFeaturedBadgesRequest(
    val featuredBadgeCodes: List<String>
)
