package com.tripping.app.data.response

// 회원가입 거주 지역 선택 등에 쓰는 전체 지역 목록 - GET /regions
data class RegionResponse(
    val regionId: String,
    val regionName: String,
    val regionType: String
)
