package com.tripping.app.data.api

import com.tripping.app.data.response.CurrentTripSummaryResponse
import retrofit2.Response
import retrofit2.http.GET

// 홈 화면 전용 API. 진행 중인 여행이 없으면 204 No Content.
interface HomeApi {

    @GET("trips/current-summary")
    suspend fun getCurrentTripSummary(): Response<CurrentTripSummaryResponse>
}
