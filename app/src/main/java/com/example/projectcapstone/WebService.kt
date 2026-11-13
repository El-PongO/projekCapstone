package com.example.projectcapstone

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface WebService {
    @POST("api/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse

    @GET("api/dashboard")
    suspend fun getDashboard(@Header("Authorization") token: String): DashboardResponse

    @GET("api/performance")
    suspend fun getPerformance(@Header("Authorization") token: String): PerformanceResponse
}
