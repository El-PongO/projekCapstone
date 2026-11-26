package com.example.projectcapstone.data.session

import android.content.Context
import com.example.projectcapstone.data.model.AuthResult
import com.example.projectcapstone.data.model.Role
import com.example.projectcapstone.data.model.User

class SessionManager(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun save(auth: AuthResult) {
        val u = auth.user
        prefs.edit()
            .putString(KEY_TOKEN, auth.token)
            .putInt(KEY_ID, u.id)
            .putString(KEY_CODE, u.agentCode)
            .putString(KEY_NAME, u.name)
            .putString(KEY_EMAIL, u.email)
            .putString(KEY_ROLE, u.role.name)
            .apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun getUser(): User? {
        if (getToken() == null) return null
        return runCatching {
            User(
                id = prefs.getInt(KEY_ID, -1),
                agentCode = prefs.getString(KEY_CODE, null)!!,
                name = prefs.getString(KEY_NAME, null)!!,
                email = prefs.getString(KEY_EMAIL, null)!!,
                role = Role.from(prefs.getString(KEY_ROLE, null)!!)
            )
        }.getOrNull()
    }

    fun clear() = prefs.edit().clear().apply()

    private companion object {
        const val KEY_TOKEN = "token"
        const val KEY_ID = "user_id"
        const val KEY_CODE = "agent_code"
        const val KEY_NAME = "name"
        const val KEY_EMAIL = "email"
        const val KEY_ROLE = "role"
    }
}
