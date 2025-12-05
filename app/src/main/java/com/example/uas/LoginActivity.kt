package com.example.uas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.uas.data.local.AppDatabase
import com.example.uas.util.SessionManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val btnBack = findViewById<android.view.View>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val password = etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Menggunakan API Remote (Railway)
            ApiClient.instance.login(email, password).enqueue(object : retrofit2.Callback<com.example.uas.data.remote.LoginResponse> {
                override fun onResponse(
                    call: retrofit2.Call<com.example.uas.data.remote.LoginResponse>,
                    response: retrofit2.Response<com.example.uas.data.remote.LoginResponse>
                ) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val body = response.body()!!
                        // Simpan session
                        sessionManager.createLoginSession(
                            body.userId ?: 0,
                            body.name ?: "",
                            body.email ?: ""
                        )
                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Failed: ${response.body()?.error}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.example.uas.data.remote.LoginResponse>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }
}
