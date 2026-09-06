package com.tripping.app.data.response

// GET /users/me/map 응답
data class MyMapResponse(
    val pins: List<MapPinResponse>,
    val visitedPlaceCount: Int // 내가 다녀온 여행(저장한 루트 제외)에 포함된 장소 개수 (중복 제거)
)
