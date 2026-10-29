package com.example.projectcapstone.ui

import android.content.Context
import android.content.Intent
import com.example.projectcapstone.data.model.Role
import com.example.projectcapstone.data.model.User
import com.example.projectcapstone.ui.login.LoginActivity
import com.example.projectcapstone.ui.main.MainActivity
import com.example.projectcapstone.ui.upline.UplineHomeActivity

object Navigator {
    fun home(context: Context, user: User): Intent = when (user.role) {
        Role.UPLINE -> Intent(context, UplineHomeActivity::class.java)
            .putExtra(UplineHomeActivity.EXTRA_NAME, user.name)
        else -> Intent(context, MainActivity::class.java)
            .putExtra(MainActivity.EXTRA_NAME, "${user.name} (${user.role.name.lowercase()})")
    }

    fun login(context: Context): Intent = Intent(context, LoginActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
}
