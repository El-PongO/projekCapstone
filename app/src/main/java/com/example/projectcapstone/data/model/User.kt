package com.example.projectcapstone.data.model

enum class Role {
    UPLINE, DOWNLINE, ADMIN;

    companion object {
        fun from(value: String): Role = valueOf(value.uppercase())
    }
}

data class User(
    val id: Int,
    val agentCode: String,
    val name: String,
    val email: String,
    val role: Role
)
