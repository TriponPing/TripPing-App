package com.tripping.app.data.api

import com.tripping.app.data.response.RouteMapSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface RouteApi {
    @GET("routes/map/search")
    suspend fun searchRoutesForMap(
        @Query("regionId") regionId: String?,
        @Query("category") category: String?
    ): List<RouteMapSearchResponse>
}