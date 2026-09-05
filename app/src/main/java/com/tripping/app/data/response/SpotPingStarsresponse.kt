// [파일 설명] 장소별 핑 통계 조회(GET /spots/{spotId}/ping-stats) 응답. 그 장소의 인기 시간대와 총 핑 개수를 담음.
package com.tripping.app.data.response

data class SpotPingStatsResponse(
    val popularTimeSlot: String,
    val totalPingCount: Long
)