// [파일 설명] Ping 후기 응답. GET/POST/PATCH /pings/{pingId}/review
package com.tripping.app.data.response

data class PingReviewResponse(
    val pingId: Long,
    val rating: Int?,
    val photoUrl: String?,
    val reviewComment: String?,
    val tags: List<String>,
    val updatedAt: String?
)