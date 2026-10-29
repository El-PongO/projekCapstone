package com.example.projectcapstone.ui.main

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.projectcapstone.R
import com.example.projectcapstone.data.session.SessionManager
import com.example.projectcapstone.ui.Navigator

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        findViewById<TextView>(R.id.welcome_text).text = getString(R.string.welcome, name)
        findViewById<Button>(R.id.logout_button).setOnClickListener {
            SessionManager(this).clear()
            startActivity(Navigator.login(this))
        }
    }

    companion object {
        const val EXTRA_NAME = "extra_name"
    }
}
