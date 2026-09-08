package com.tripping.app.data.api

import com.tripping.app.data.request.RouteRecommendRequest
import com.tripping.app.data.response.RouteCandidateResponse
import com.tripping.app.data.response.RouteMapSearchResponse
import com.tripping.app.data.response.PlaceSearchResponse
import com.tripping.app.data.response.PlannedRouteResponse
import com.tripping.app.data.response.PlannedRoutePlaceResponse
import com.tripping.app.data.request.PlannedRouteCreateRequest
import com.tripping.app.data.request.PlannedRoutePlaceRequest
import com.tripping.app.data.request.TripCreateRequest
import com.tripping.app.data.response.TripResponse
import retrofit2.http.Path
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface RouteApi {
    @GET("routes/map/search")
    suspend fun searchRoutesForMap(
        @Query("regionId") regionId: String?,
        @Query("category") category: String?
    ): List<RouteMapSearchResponse>

    @POST("routes/recommend")
    suspend fun recommendRoutes(
        @Body request: RouteRecommendRequest
    ): List<RouteCandidateResponse>

    @GET("places/search")
    suspend fun searchPlaces(
        @Query("query") query: String,
        @Query("regionId") regionId: String? = null
    ): List<PlaceSearchResponse>

    @GET("places/saved/me")
    suspend fun getSavedPlaces(): List<PlaceSearchResponse>

    @POST("routes")
    suspend fun createPlannedRoute(
        @Body request: PlannedRouteCreateRequest
    ): PlannedRouteResponse

    @POST("routes/{routeId}/places")
    suspend fun addPlannedRoutePlace(
        @Path("routeId") routeId: Long,
        @Body request: PlannedRoutePlaceRequest
    ): PlannedRoutePlaceResponse

    @POST("routes/{routeId}/trips")
    suspend fun createTrip(
        @Path("routeId") routeId: Long,
        @Body request: TripCreateRequest
    ): TripResponse

}