package com.tripping.app.data.request

// PATCH /users/me 요청 - null인 필드는 서버에서 수정 안 함(부분 수정)
data class ProfileUpdateRequest(
    val nickname: String? = null,
    val profileImage: String? = null,
    val regionId: String? = null,
    val language: String? = null
)
