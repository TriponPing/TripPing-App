// [파일 설명] Ping(여행 중 방문 장소 기록) 관련 서버 통신 담당. 핑 등록/조회, 핑 후기 등록·수정·삭제 API를 정의함.
package com.tripping.app.data.api

import com.tripping.app.data.request.AddTripSpotRequest
import com.tripping.app.data.request.CreatePingRequest
import com.tripping.app.data.request.PingReviewRequest
import com.tripping.app.data.response.AddTripSpotResponse
import com.tripping.app.data.response.CurrentTripResponse
import com.tripping.app.data.response.PingDto
import com.tripping.app.data.response.PingResponse
import com.tripping.app.data.response.PingReviewResponse
import com.tripping.app.data.response.SpotPingStatsResponse
import retrofit2.http.*

interface PingApi {

    // 현재 진행 중인 여행 조회 (id 없이 스스로 조회)
    @GET("routes/current")
    suspend fun getCurrentInProgressRoute(): CurrentTripResponse

    // 진행 중 여행 조회 (routeId 기준 진행 중인 Ping 목록)
    @GET("routes/{routeId}/pings")
    suspend fun getOngoingPings(@Path("routeId") routeId: Long): PingResponse

    // 방문 장소 Ping 등록 - 진행 중인 여행 전용 (WIDGET_PING에 저장)
    @POST("routes/{routeId}/pings")
    suspend fun createPing(
        @Path("routeId") routeId: Long,
        @Body request: CreatePingRequest
    ): PingDto

    // 여행 Ping 기록 조회 (완료된 여행의 전체 핑 기록)
    @GET("trips/{routeId}/pings")
    suspend fun getTripPingHistory(@Path("routeId") routeId: Long): List<PingDto>

    // 👈 새로 추가: 완료된 여행에 놓친 방문 스팟 추가 - ACTUAL_ROUTE_SPOT에 바로 저장
    @POST("trips/{routeId}/spots")
    suspend fun addTripSpot(
        @Path("routeId") routeId: Long,
        @Body request: AddTripSpotRequest
    ): AddTripSpotResponse

    // 👈 새로 추가: 여행 기록(방문 스팟) 삭제 - 실수로 잘못 찍은 기록 지우기
    @DELETE("trips/{routeId}/spots/{actualRouteSpotId}")
    suspend fun deleteTripSpot(
        @Path("routeId") routeId: Long,
        @Path("actualRouteSpotId") actualRouteSpotId: Long
    )

    // 👈 새로 추가: 기존 후기 조회 - 화면 진입 시 등록/수정 모드 판단용 (없으면 404 예외 발생함)
    @GET("pings/{pingId}/review")
    suspend fun getPingReview(@Path("pingId") pingId: Long): PingReviewResponse

    // Ping 후기 등록
    @POST("pings/{pingId}/review")
    suspend fun createPingReview(
        @Path("pingId") pingId: Long,
        @Body request: PingReviewRequest
    )

    // Ping 후기 삭제
    @DELETE("pings/{pingId}/review")
    suspend fun deletePingReview(@Path("pingId") pingId: Long)

    // Ping 후기 수정
    @PATCH("pings/{pingId}/review")
    suspend fun updatePingReview(
        @Path("pingId") pingId: Long,
        @Body request: PingReviewRequest
    )

    // 장소별 핑 통계 조회
    @GET("spots/{spotId}/ping-stats")
    suspend fun getSpotPingStats(
        @Path("spotId") spotId: Long
    ): SpotPingStatsResponse
}