package com.tripping.app.data.request

// ⚠️ 실제 TripCreateRequest.java 필드/타입 확인되면 다시 맞춰야 함 (추측으로 작성)
data class TripCreateRequest(
    val travelDate: String,       // "yyyy-MM-dd" 형식
    val companionType: String?,
    val transport: String?,
    val memberCount: Int?
)