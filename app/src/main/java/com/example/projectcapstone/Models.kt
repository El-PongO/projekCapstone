package com.example.projectcapstone

import com.squareup.moshi.Json

// Request dan Response dipisah menjadi data class, seperti pada tutor.
data class LoginRequest(val identifier: String, val password: String)
data class LoginResponse(val token: String, val user: AgencyUser)
data class LoginSession(val token: String, val user: AgencyUser)

data class AgencyUser(
    val id: Int,
    val agentCode: String,
    val name: String,
    val email: String,
    val role: String
)

data class DashboardStats(
    val totalProjects: Int,
    val dealCount: Int,
    val followUpCount: Int,
    val dealValue: Double,
    val agentCount: Int,
    val myCommission: Double
)

data class ProjectSummary(
    val projectCode: String,
    val title: String,
    val status: String,
    val stage: String,
    val dealValue: Double
)

data class DashboardResponse(
    val role: String,
    val stats: DashboardStats,
    val recentProjects: List<ProjectSummary>
)

data class DashboardData(
    val stats: DashboardStats,
    val recentProjects: List<ProjectSummary>
)

data class SalesReport(
    @Json(name = "projectCode") val code: String,
    @Json(name = "clientName") val client: String,
    val status: String,
    val stage: String,
    @Json(name = "dealValue") val value: Double,
    val date: String
)

data class IncomeReport(
    @Json(name = "projectCode") val code: String,
    @Json(name = "clientName") val client: String,
    val commission: Double,
    @Json(name = "ratePercent") val rate: Double,
    val date: String
)

data class PerformanceStats(val totalSales: Double, val totalIncome: Double)
data class PerformanceResponse(
    val stats: PerformanceStats,
    val sales: List<SalesReport>,
    val income: List<IncomeReport>
)

data class PerformanceData(
    val totalSales: Double,
    val totalIncome: Double,
    val sales: List<SalesReport>,
    val income: List<IncomeReport>
)
