package com.example.projectcapstone.ui.login

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.projectcapstone.R
import com.example.projectcapstone.data.repository.ApiException
import com.example.projectcapstone.data.repository.ApiUserRepository
import com.example.projectcapstone.data.repository.UserRepository
import com.example.projectcapstone.data.session.SessionManager
import com.example.projectcapstone.ui.Navigator
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class LoginActivity : AppCompatActivity() {

    private val repository: UserRepository = ApiUserRepository
    private lateinit var session: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        session = SessionManager(this)

        // Already logged in: skip straight to the role's home screen.
        session.getUser()?.let { user ->
            startActivity(Navigator.home(this, user))
            finish()
            return
        }

        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login_root)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val usernameLayout = findViewById<TextInputLayout>(R.id.username_layout)
        val passwordLayout = findViewById<TextInputLayout>(R.id.password_layout)
        val usernameInput = findViewById<TextInputEditText>(R.id.username_input)
        val passwordInput = findViewById<TextInputEditText>(R.id.password_input)

        val loginButton = findViewById<MaterialButton>(R.id.login_button)

        fun attemptLogin() {
            val username = usernameInput.text?.toString().orEmpty().trim()
            val password = passwordInput.text?.toString().orEmpty()
            usernameLayout.error = if (username.isEmpty()) getString(R.string.error_username_required) else null
            passwordLayout.error = if (password.isEmpty()) getString(R.string.error_password_required) else null
            if (username.isEmpty() || password.isEmpty()) return

            loginButton.isEnabled = false
            repository.login(username, password) { result ->
                loginButton.isEnabled = true
                result.onSuccess { auth ->
                    session.save(auth)
                    startActivity(Navigator.home(this, auth.user))
                    finish()
                }.onFailure { e ->
                    passwordLayout.error = if (e is ApiException && e.unauthorized) {
                        getString(R.string.error_invalid_credentials)
                    } else {
                        getString(R.string.error_network)
                    }
                }
            }
        }

        loginButton.setOnClickListener { attemptLogin() }
        passwordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else false
        }

        findViewById<View>(R.id.contact_us).setOnClickListener {
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${getString(R.string.contact_email)}"))
            try {
                startActivity(intent)
            } catch (e: android.content.ActivityNotFoundException) {
                Toast.makeText(this, R.string.no_email_app, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
