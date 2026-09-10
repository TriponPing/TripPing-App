// [파일 설명] Ping "로그" 탭(로그 커뮤니티) 관련 서버 통신 - 지역별 공개 루트 목록, 루트 후기 조회 담당.
package com.tripping.app.data.api

import com.tripping.app.data.response.RouteReviewResponse
import com.tripping.app.data.response.RouteSummaryResponse
import com.tripping.app.data.response.SpringPageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CommunityApi {

    // 특정 지역을 지나간 공개 루트 목록 (로그 커뮤니티 리스트)
    @GET("regions/{regionId}/routes")
    suspend fun getRoutesByRegion(
        @Path("regionId") regionId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<SpringPageResponse<RouteSummaryResponse>>

    // 특정 루트에 달린 후기 목록 (로그 상세보기)
    @GET("routes/{routeId}/reviews")
    suspend fun getRouteReviews(
        @Path("routeId") routeId: Long,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<SpringPageResponse<RouteReviewResponse>>
}
