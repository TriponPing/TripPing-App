package com.tripping.app.data.response

data class PopularPlaceResponse(
    val spotId: Long,
    val name: String,
    val category: String?,
    val address: String?,
    val savedCount: Long,
    // 👈 새로 추가: 저장 많이 된 루트의 이 장소 후기 사진 (없으면 null -> 프론트에서 자리별 고정 이미지로 대체)
    val photoUrl: String? = null
)
