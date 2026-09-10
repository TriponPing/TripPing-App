package com.tripping.app.data.api

import com.tripping.app.data.response.RegionResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface RegionApi {

    // 회원가입 "사는 지역" 선택 그리드용 - 전체 지역 목록
    @GET("regions")
    suspend fun getAllRegions(): Response<List<RegionResponse>>

    @GET("regions/search")
    suspend fun searchRegions(@Query("keyword") keyword: String): Response<List<RegionResponse>>
}
