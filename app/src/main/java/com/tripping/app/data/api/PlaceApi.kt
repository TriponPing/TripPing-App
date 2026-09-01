package com.tripping.app.data.api

import com.tripping.app.data.response.PlaceSearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PlaceApi {
    @GET("map/places/search")
    suspend fun searchPlaces(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double
    ): List<PlaceSearchResponse>
}