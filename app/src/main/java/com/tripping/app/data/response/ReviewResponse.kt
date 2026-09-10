// [파일 설명] "로그 상세보기" 화면 - 루트에 달린 후기 목록. GET /routes/{routeId}/reviews
package com.tripping.app.data.response

data class RouteReviewResponse(
    val reviewId: Long,
    val routeId: Long,
    val writerId: Long,
    val writerNickname: String,
    val content: String,
    val createdAt: String?,
    val updatedAt: String?
)
