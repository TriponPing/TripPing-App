// [파일 설명] 새 장소 등록 API(POST /places) 요청 바디. 네이버맵에서 새로 발견한(아직 우리 DB에 없는) 장소를 등록할 때 씀.
package com.tripping.app.data.request

data class CreatePlaceRequest(
    val name: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val regionId: String? = null // 지역핑 등록 흐름에서 새 장소를 만들 때만 채워서 보냄
)