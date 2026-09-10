package com.tripping.app.data.request

// POST /region-pings 요청 - 본인 거주 지역 장소에 대한 평점/후기 등록
// spotId는 기존 장소를 골랐으면 그 spotId, 새 장소면 먼저 POST /places(createPlace)로 등록한 뒤 받은 spotId를 그대로 씀.
data class RegionPingRequest(
    val spotId: Long,
    val rating: Int,
    val reviewComment: String?
)
