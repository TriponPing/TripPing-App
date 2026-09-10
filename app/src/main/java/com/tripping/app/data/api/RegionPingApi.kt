package com.tripping.app.data.api

import com.tripping.app.data.request.RegionPingRequest
import com.tripping.app.data.response.RegionPingResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface RegionPingApi {
    // 지역핑 등록 - 본인 거주 지역(회원가입 때 고른 regionId)과 장소의 regionId가 같아야만 성공함 (403)
    @POST("region-pings")
    suspend fun registerRegionPing(@Body request: RegionPingRequest): RegionPingResponse
}
