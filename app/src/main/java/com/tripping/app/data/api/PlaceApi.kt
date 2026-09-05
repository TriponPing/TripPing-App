package com.tripping.app.data.api

import com.tripping.app.data.request.CreatePlaceRequest
import com.tripping.app.data.response.PlaceSearchResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface PlaceApi {
    @GET("map/places/search")
    suspend fun searchPlaces(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("radius") radius: Double
    ): List<PlaceSearchResponse>

    @GET("places")
    suspend fun getPlaces(
        @Query("category") category: String,
        @Query("regionId") regionId: String?
    ): List<PlaceSearchResponse>

    // 👈 새로 추가: 네이버맵에서 발견한(우리 DB에 없는) 장소를 새로 등록 - 백엔드팀 구현 예정
    @POST("places")
    suspend fun createPlace(@Body request: CreatePlaceRequest): PlaceSearchResponse
}