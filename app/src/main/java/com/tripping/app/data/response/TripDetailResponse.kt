// [파일 설명] 여행 기록 상세 조회 응답. GET /users/me/trips/{tripId}
package com.tripping.app.data.response

data class TripDetailResponse(
    val tripId: Long,
    val travelDate: String,
    val companionType: String?,
    val transport: String?,
    val memberCount: Int?,
    val status: String?,
    val isPublic: Boolean?,
    val spots: List<SpotDetail>
) {
    data class SpotDetail(
        val actualRouteSpotId: Long?, // 👈 새로 추가: 후기(Ping 로그) 등록/수정 API의 pingId로 씀
        val visitOrder: Int?,
        val spotId: Long?,
        val spotName: String?,
        val category: String?,
        val address: String?,
        val latitude: Double?,
        val longitude: Double?,
        val visitTime: String?,
        val rating: Int?,
        val photoUrl: String?,
        val reviewComment: String?
    )
}