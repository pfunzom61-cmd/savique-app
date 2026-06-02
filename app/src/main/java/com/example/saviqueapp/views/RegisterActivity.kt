package com.example.saviqueapp.views

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.saviqueapp.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private val TAG = "SAVIQUE_REGISTER"
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.btnRegisterUser.setOnClickListener {
            val email = binding.etRegUsername.text.toString().trim()
            val password = binding.etRegPassword.text.toString().trim()

            // DEFENSIVE: Validate before touching Firebase
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            binding.btnRegisterUser.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    Log.d(TAG, "Firebase Auth account created. UID: $uid")

                    // Save user profile to Firestore so we can identify them
                    val userDoc = hashMapOf(
                        "email" to email,
                        "streak" to 0,        // Custom Feature 1: spending streak starts at 0
                        "lastLogDate" to ""   // Tracks last day user logged an expense
                    )

                    db.collection("users").document(uid).set(userDoc)
                        .addOnSuccessListener {
                            Log.d(TAG, "User profile saved to Firestore")
                            Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Firestore profile save failed: ${e.message}")
                            Toast.makeText(this, "Account created but profile save failed. You can still log in.", Toast.LENGTH_LONG).show()
                            finish()
                        }
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "Registration failed: ${e.message}")
                    Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                    binding.btnRegisterUser.isEnabled = true
                }
        }

        binding.tvBackToLogin.setOnClickListener {
            finish()
        }
    }
}