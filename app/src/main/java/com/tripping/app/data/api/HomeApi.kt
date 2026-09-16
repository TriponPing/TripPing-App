package com.tripping.app.data.api

import com.tripping.app.data.response.CurrentTripSummaryResponse
import com.tripping.app.data.response.PageResponse
import com.tripping.app.data.response.PopularKeywordResponse
import com.tripping.app.data.response.PopularPlaceResponse
import com.tripping.app.data.response.PopularTripResponse
import com.tripping.app.data.response.SavedPlaceResponse
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

// 홈 화면 전용 API. 진행 중인 여행이 없으면 204 No Content.
interface HomeApi {

    @GET("trips/current-summary")
    suspend fun getCurrentTripSummary(): Response<CurrentTripSummaryResponse>

    @GET("trips/popular")
    suspend fun getPopularTrips(
        @Query("period") period: String = "week",
        @Query("limit") limit: Int = 10
    ): Response<List<PopularTripResponse>>

    @GET("keyword/popular")
    suspend fun getPopularKeywords(
        @Query("limit") limit: Int = 10
    ): Response<List<PopularKeywordResponse>>

    @GET("keyword/{keyword}/routes")
    suspend fun getRoutesByKeyword(
        @Path("keyword") keyword: String,
        @Query("limit") limit: Int = 20
    ): Response<List<PopularTripResponse>>

    @GET("places/popular")
    suspend fun getPopularPlaces(
        @Query("limit") limit: Int = 30
    ): Response<List<PopularPlaceResponse>>

    // 👈 새로 추가: "내 주변 코스" - 진행 중인 여행이 있으면 마지막으로 찍은 핑 좌표, 없으면 기기 현재 위치로 호출
    @GET("trips/nearby")
    suspend fun getNearbyTrips(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radiusKm") radiusKm: Double? = null,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<PopularTripResponse>>

    @GET("places/saved/me/ids")
    suspend fun getSavedPlaceIds(): Response<List<Long>>

    @POST("places/{placeId}/saved/me")
    suspend fun savePlace(@Path("placeId") placeId: Long): Response<SavedPlaceResponse>

    @DELETE("places/{placeId}/saved/me")
    suspend fun unsavePlace(@Path("placeId") placeId: Long): Response<SavedPlaceResponse>
}
