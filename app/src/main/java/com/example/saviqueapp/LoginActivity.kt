package com.example.saviqueapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saviqueapp.databinding.ActivityLoginBinding
import com.example.saviqueapp.viewmodels.UserViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Connect to our Logic Layer

    private val userViewModel: UserViewModel by viewModels {
        ViewModelFactory((application as SaviqueApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use ViewBinding for "Easy to debug" and "Scalable" code
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Feature: Log in logic
        binding.btnLogin.setOnClickListener {
            val username = binding.etUsername.text.toString()
            val password = binding.etPassword.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                // Handle invalid inputs
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
            } else {
                userViewModel.login(username, password)
            }
        }

        // Observe the login status from our ViewModel
        userViewModel.loginStatus.observe(this) { success ->
            when (success) {
                true -> {
                    // Success! Navigate to the Main Dashboard
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Close login so user can't "Go Back" to it
                }
                false -> {
                    Toast.makeText(this, "Invalid Username or Password", Toast.LENGTH_SHORT).show()
                }
                null -> { /* Do nothing */ }
            }
        }

        // Change this block at the bottom of LoginActivity.kt
        binding.tvRegisterLink.setOnClickListener {
            // This stops creating a test user and actually opens your Register screen
            val intent = Intent(this, com.example.saviqueapp.views.RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}