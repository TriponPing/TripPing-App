// [파일 설명] Ping "로그" 탭(로그 커뮤니티) - 지역별 공개 루트 목록. GET /regions/{regionId}/routes
package com.tripping.app.data.response

data class RouteSummaryResponse(
    val routeId: Long,
    val writerNickname: String,
    val travelDate: String?,
    val companionType: String?,
    val transport: String?,
    val memberCount: Int?,
    val status: String?,
    val createdAt: String?,
    val spotNames: List<String>,
    val spotCount: Int,
    val savedCount: Long
)
