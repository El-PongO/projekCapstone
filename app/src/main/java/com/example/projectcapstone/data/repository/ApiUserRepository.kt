package com.example.projectcapstone.data.repository

import android.os.Handler
import android.os.Looper
import com.example.projectcapstone.BuildConfig
import com.example.projectcapstone.data.model.AuthResult
import com.example.projectcapstone.data.model.Role
import com.example.projectcapstone.data.model.User
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class ApiException(message: String, val unauthorized: Boolean = false) : IOException(message)

object ApiUserRepository : UserRepository {

    private val executor = Executors.newSingleThreadExecutor()
    private val mainHandler = Handler(Looper.getMainLooper())

    override fun login(identifier: String, password: String, callback: (Result<AuthResult>) -> Unit) {
        executor.execute {
            val result = runCatching { requestLogin(identifier, password) }
                .onFailure { android.util.Log.e("ApiUserRepository", "login failed", it) }
            mainHandler.post { callback(result) }
        }
    }

    private fun requestLogin(identifier: String, password: String): AuthResult {
        val conn = URL(BuildConfig.API_BASE_URL + "api/auth/login").openConnection() as HttpURLConnection
        try {
            conn.requestMethod = "POST"
            conn.connectTimeout = 8000
            conn.readTimeout = 8000
            conn.doOutput = true
            conn.setRequestProperty("Content-Type", "application/json")
            val body = JSONObject().put("identifier", identifier).put("password", password).toString()
            conn.outputStream.use { it.write(body.toByteArray()) }

            val code = conn.responseCode
            val text = (if (code in 200..299) conn.inputStream else conn.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (code == 401) throw ApiException("Invalid credentials", unauthorized = true)
            if (code !in 200..299) throw ApiException("Server error ($code)")

            val json = JSONObject(text)
            val u = json.getJSONObject("user")
            return AuthResult(
                json.getString("token"),
                User(
                    id = u.getInt("id"),
                    agentCode = u.getString("agentCode"),
                    name = u.getString("name"),
                    email = u.getString("email"),
                    role = Role.from(u.getString("role"))
                )
            )
        } finally {
            conn.disconnect()
        }
    }
}
