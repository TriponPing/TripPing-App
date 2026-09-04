package com.tripping.app.ui.model

// ===== "기록" 탭에서 쓸 모델 =====
data class PingItem(
    val id: Int,
    val placeName: String,
    val time: String,
    val status: PingStatus
)

enum class PingStatus { DONE, CURRENT, UPCOMING }

// ===== "로그" 탭에서 쓸 모델 (지역별 코스 카드) =====
data class PingLogCourse(
    val id: Int,
    val authorName: String,
    val courseName: String,
    val stops: List<String>,    // 예: ["강남", "코엑스", "석촌호수"]
    val pingCount: Int,
    val bookmarkCount: Int,
    val distanceKm: Double
)