package com.tripping.app.data.api

import com.tripping.app.data.request.ProfileUpdateRequest
import com.tripping.app.data.request.UpdateFeaturedBadgesRequest
import com.tripping.app.data.response.BadgeResponse
import com.tripping.app.data.response.MapDetailResponse
import com.tripping.app.data.response.MapPinResponse
import com.tripping.app.data.response.MapSearchResponse
import com.tripping.app.data.response.PageResponse
import com.tripping.app.data.response.ProfileResponse
import com.tripping.app.data.response.SavedRouteResponse
import com.tripping.app.data.response.TripDetailResponse
import com.tripping.app.data.response.TripSummaryResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface MyPageApi {

    @GET("users/me")
    suspend fun getProfile(): Response<ProfileResponse>

    @PATCH("users/me")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<ProfileResponse>

    @GET("users/me/trips/recent")
    suspend fun getRecentTrips(): Response<List<TripSummaryResponse>>

    @GET("users/me/trips")
    suspend fun getAllTrips(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<TripSummaryResponse>>

    // 다녀온 여행 상세보기 ("코스 상세" 화면 - 지도, 여행 바로 시작하기 등에 사용)
    @GET("users/me/trips/{tripId}")
    suspend fun getTripDetail(@Path("tripId") tripId: Long): Response<TripDetailResponse>

    @GET("users/me/routes/saved")
    suspend fun getSavedRoutes(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 10
    ): Response<PageResponse<SavedRouteResponse>>

    // routeId = SavedRouteResponse.tripId (원본 actualRouteId)
    @DELETE("routes/{routeId}/saved")
    suspend fun unsaveRoute(@Path("routeId") routeId: Long): Response<Unit>

    @GET("users/me/map")
    suspend fun getMyMap(): Response<List<MapPinResponse>>

    // type = "drawn"(다녀온 여행) | "saved"(저장한 루트)
    @GET("users/me/map/detail")
    suspend fun getMyMapDetail(@Query("type") type: String): Response<List<MapDetailResponse>>

    @GET("users/me/map/search")
    suspend fun searchMyMap(@Query("keyword") keyword: String): Response<List<MapSearchResponse>>

    @GET("users/me/badges")
    suspend fun getBadges(): Response<List<BadgeResponse>>

    @PUT("users/me/badges/featured")
    suspend fun updateFeaturedBadges(@Body request: UpdateFeaturedBadgesRequest): Response<List<BadgeResponse>>
}
