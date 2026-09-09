// [파일 설명] Ping 후기 등록/수정 요청. 백엔드 스펙(rating 필수, photoUrl/reviewComment/tags 선택)에 맞춤.
package com.tripping.app.data.request

data class PingReviewRequest(
    val rating: Int,
    val photoUrl: String? = null,
    val reviewComment: String? = null,
    val tags: List<String>? = null
)