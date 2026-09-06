package com.tripping.app.data.api

import com.tripping.app.data.response.CurrentTripSummaryResponse
import com.tripping.app.data.response.PopularKeywordResponse
import com.tripping.app.data.response.PopularPlaceResponse
import com.tripping.app.data.response.PopularTripResponse
import retrofit2.Response
import retrofit2.http.GET
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

    @GET("places/popular")
    suspend fun getPopularPlaces(
        @Query("limit") limit: Int = 30
    ): Response<List<PopularPlaceResponse>>
}
