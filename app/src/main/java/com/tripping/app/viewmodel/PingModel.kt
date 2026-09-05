package com.tripping.app.viewmodel

enum class PingStatus { DONE, CURRENT, UPCOMING }

data class PingItem(
    val id: Long, // 👈 Int -> Long으로 변경!
    val placeName: String,
    val time: String,
    val status: PingStatus
)

// PingLogContent에서 사용하는 구조에 딱 맞춘 모델
data class PingLogCourse(
    val id: Long, // 👈 여기도 필요하다면 Long으로 맞춰주면 안전합니다!
    val authorName: String,
    val courseName: String,
    val stops: List<String>,
    val pingCount: Int,
    val distanceKm: Int,
    val bookmarkCount: Int
)