package com.tripping.app.data.api

import com.tripping.app.data.request.LoginRequest
import com.tripping.app.data.request.SignUpRequest
import com.tripping.app.data.response.MeResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/join")
    suspend fun signUp(@Body request: SignUpRequest): Response<String>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<String>

    @POST("auth/logout")
    suspend fun logout(): Response<String>

    @GET("auth/me")
    suspend fun getMe(): Response<MeResponse>
}