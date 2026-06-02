package com.example.saviqueapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saviqueapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private val TAG = "SAVIQUE_LOGIN"
    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialise Firebase Auth
        auth = FirebaseAuth.getInstance()

        // DEFENSIVE: If user is already logged in, skip login screen
        if (auth.currentUser != null) {
            Log.d(TAG, "User already logged in, skipping to MainActivity")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // DEFENSIVE: Validate inputs before sending to Firebase
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter all details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Show feedback while Firebase works
            binding.btnLogin.isEnabled = false

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.d(TAG, "Login successful for: $email")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Login failed: ${e.message}")
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.btnLogin.isEnabled = true
                }
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, com.example.saviqueapp.views.RegisterActivity::class.java))
        }
    }
}