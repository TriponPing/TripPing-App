package com.tripping.app.data.api

import com.tripping.app.data.request.PingReviewRequest
import com.tripping.app.data.response.PingResponse
import retrofit2.http.*

interface PingApi {

    // 진행 중 여행 조회 (routeId 기준 진행 중인 Ping 목록)
    @GET("routes/{routeId}/pings")
    suspend fun getOngoingPings(@Path("routeId") routeId: Int): List<PingResponse>

    // 방문 장소 Ping 등록
    @POST("routes/{routeId}/pings")
    suspend fun createPing(@Path("routeId") routeId: Int): PingResponse

    // 여행 Ping 기록 조회 (완료된 여행의 전체 핑 기록)
    @GET("trips/{routeId}/pings")
    suspend fun getTripPingHistory(@Path("routeId") routeId: Int): List<PingResponse>

    // Ping 후기 등록
    @POST("pings/{pingId}/review")
    suspend fun createPingReview(
        @Path("pingId") pingId: Int,
        @Body request: PingReviewRequest
    )

    // Ping 후기 삭제
    @DELETE("pings/{pingId}/review")
    suspend fun deletePingReview(@Path("pingId") pingId: Int)

    // Ping 후기 수정
    @PATCH("pings/{pingId}/review")
    suspend fun updatePingReview(
        @Path("pingId") pingId: Int,
        @Body request: PingReviewRequest
    )
}