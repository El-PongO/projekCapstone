package com.example.projectcapstone

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

data class TeamSummary(
    val downlineCount: Int = 0,
    val totalDeals: Int = 0
)

data class TeamDownline(
    val id: Long,
    val agentCode: String,
    val joinedAt: String? = null,
    val totalDeals: Int = 0,
    val dealsPerMonth: Double = 0.0
)

data class TeamResponse(
    val summary: TeamSummary,
    val downlines: List<TeamDownline>
)

data class TeamProject(
    val id: Long,
    val code: String,
    val status: String,
    val stage: String,
    val dealValue: Double = 0.0,
    val updatedAt: String? = null
)

data class TeamDetailResponse(
    val downline: TeamDownline,
    val projects: List<TeamProject>
)

interface UplineDownlineWebService {

    @GET("api/upline/downlines")
    fun getDownlines(
        @Header("Authorization") token: String
    ): Call<TeamResponse>

    @GET("api/upline/downlines/{id}")
    fun getDownlineDetail(
        @Header("Authorization") token: String,
        @Path("id") id: Long
    ): Call<TeamDetailResponse>
}

object UplineDownlineApi {

    // Untuk Android Emulator dengan ADB Reverse aktif.
    // Sementara disamakan dengan URL Backend kamu.
    private const val BASE_URL = "http://127.0.0.1:3000/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    val service: UplineDownlineWebService =
        retrofit.create(UplineDownlineWebService::class.java)
}