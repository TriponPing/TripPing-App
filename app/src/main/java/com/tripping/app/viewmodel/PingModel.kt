package com.tripping.app.viewmodel

enum class PingStatus { DONE, CURRENT, UPCOMING }

data class PingItem(
    val id: Long, // 👈 Int -> Long으로 변경!
    val placeName: String,
    val time: String,
    val status: PingStatus
)
