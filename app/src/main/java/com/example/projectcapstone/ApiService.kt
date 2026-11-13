package com.example.projectcapstone

import android.content.Context
import android.util.Log
import java.io.IOException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import retrofit2.HttpException

class ApiError(message: String, val unauthorized: Boolean = false) : IOException(message)

object ApiService {
    // Di sini hanya pemanggilan WebService; tidak membuat HttpURLConnection manual.
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun errorMessage(error: Exception): ApiError {
        if (error is HttpException) {
            val body = runCatching { error.response()?.errorBody()?.string() }.getOrNull().orEmpty()
            val message = runCatching { JSONObject(body).optString("error") }.getOrNull()
                ?.ifBlank { null } ?: "HTTP ${error.code()}"
            return ApiError(message, unauthorized = error.code() == 401)
        }
        return ApiError(error.message ?: "Koneksi gagal")
    }

    fun login(identifier: String, password: String, done: (Result<LoginSession>) -> Unit) {
        scope.launch {
            if (BuildConfig.DEBUG) Log.d("IntegraLogin", "LOGIN_START")
            val result = try {
                val response = App.retrofitService.login(LoginRequest(identifier, password))
                if (BuildConfig.DEBUG) Log.d("IntegraLogin", "LOGIN_OK role=${response.user.role}")
                Result.success(LoginSession(response.token, response.user))
            } catch (error: Exception) {
                if (BuildConfig.DEBUG) {
                    Log.e("IntegraLogin", "LOGIN_FAILED ${error.javaClass.simpleName}: ${error.message}", error)
                }
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun dashboard(token: String, done: (Result<DashboardData>) -> Unit) {
        scope.launch {
            val result = try {
                val response = App.retrofitService.getDashboard("Bearer $token")
                Result.success(DashboardData(response.stats, response.recentProjects))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }

    fun performance(token: String, done: (Result<PerformanceData>) -> Unit) {
        scope.launch {
            val result = try {
                val response = App.retrofitService.getPerformance("Bearer $token")
                Result.success(PerformanceData(
                    response.stats.totalSales, response.stats.totalIncome,
                    response.sales, response.income
                ))
            } catch (error: Exception) {
                Result.failure(errorMessage(error))
            }
            withContext(Dispatchers.Main) { done(result) }
        }
    }
}

class SessionStore(context: Context) {
    // Untuk demo capstone lokal. Gunakan encrypted storage pada aplikasi production.
    private val preferences = context.applicationContext
        .getSharedPreferences("integra_session", Context.MODE_PRIVATE)

    fun save(session: LoginSession) {
        val user = session.user
        preferences.edit()
            .putString("token", session.token)
            .putInt("id", user.id)
            .putString("agentCode", user.agentCode)
            .putString("name", user.name)
            .putString("email", user.email)
            .putString("role", user.role)
            .apply()
    }

    fun restore(): LoginSession? {
        val token = preferences.getString("token", null) ?: return null
        return LoginSession(token, AgencyUser(
            id = preferences.getInt("id", -1),
            agentCode = preferences.getString("agentCode", "").orEmpty(),
            name = preferences.getString("name", "").orEmpty(),
            email = preferences.getString("email", "").orEmpty(),
            role = preferences.getString("role", "").orEmpty()
        ))
    }

    fun clear() = preferences.edit().clear().apply()
}
