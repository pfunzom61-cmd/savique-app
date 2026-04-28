package com.example.saviqueapp.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.saviqueapp.data.Repository

class ViewModelFactory(private val repository: Repository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(BudgetViewModel::class.java) ->
                BudgetViewModel(repository) as T
            modelClass.isAssignableFrom(UserViewModel::class.java) ->
                UserViewModel(repository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}