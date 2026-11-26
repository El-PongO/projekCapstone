package com.example.projectcapstone.data.repository

import com.example.projectcapstone.data.model.AuthResult

interface UserRepository {
    // Runs off the main thread; the callback is delivered on the main thread.
    fun login(identifier: String, password: String, callback: (Result<AuthResult>) -> Unit)
}
