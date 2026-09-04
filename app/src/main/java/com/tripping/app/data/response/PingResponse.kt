package com.tripping.app.data.response

// TODO: 실제 서버 응답 JSON 필드명에 맞춰 수정 필요
data class PingResponse(
    val id: Int,
    val placeName: String,       // 예: "ABC카페"
    val visitTime: String,       // 예: "12:30" 또는 ISO 시간 문자열
    val status: String,          // 예: "DONE", "CURRENT", "UPCOMING" (타임라인 점 색상 구분용 추정)
    val hasReview: Boolean = false
)