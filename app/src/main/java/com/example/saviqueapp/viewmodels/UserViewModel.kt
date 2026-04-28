package com.example.saviqueapp.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.saviqueapp.data.Repository
import com.example.saviqueapp.models.User
import kotlinx.coroutines.launch

class UserViewModel(private val repository: Repository) : ViewModel() {

    private val TAG = "UserViewModel"

    // This tells the UI if the login was successful or not
    val loginStatus = MutableLiveData<Boolean?>()

    fun login(username: String, password: String) {
        //Simple validation before hitting the DB
        if (username.isBlank() || password.isBlank()) {
            loginStatus.value = false
            return
        }

        viewModelScope.launch {
            try {
                val user = repository.login(username, password)
                if (user != null) {
                    Log.d(TAG, "Login successful for: $username")
                    loginStatus.value = true
                } else {
                    Log.w(TAG, "Login failed: Invalid credentials")
                    loginStatus.value = false
                }
            } catch (e: Exception) {
                Log.e(TAG, "Database error during login: ${e.message}")
                loginStatus.value = false
            }
        }
    }

    /**
     *
     * It allows any User object to be saved directly to the RoomDB.
     */
    fun register(user: User) {
        viewModelScope.launch {
            try {
                repository.register(user)
                Log.d(TAG, "User ${user.username} successfully saved to RoomDB.")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving user: ${e.message}")
            }
        }
    }

    fun registerInitialUser(username: String, password: String) {
        viewModelScope.launch {
            val newUser = User(username = username, password = password)
            repository.register(newUser)
            Log.d(TAG, "Initial user registered for testing.")
        }
    }

    // Reset status so the message doesn't pop up twice
    fun resetStatus() {
        loginStatus.value = null
    }
}