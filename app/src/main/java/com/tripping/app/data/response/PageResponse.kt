package com.tripping.app.data.response

// 마이페이지 목록 API(페이징) 공용 응답 - GET /users/me/trips, GET /users/me/routes/saved 등
data class PageResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val last: Boolean
)
