package com.example.saviqueapp.viewmodels

import android.util.Log
import androidx.lifecycle.*
import com.example.saviqueapp.data.Repository
import com.example.saviqueapp.models.*
import kotlinx.coroutines.launch

class BudgetViewModel(private val repository: Repository) : ViewModel() {

    private val TAG = "BudgetViewModel"

    // --- CATEGORIES ---
    val allCategories: LiveData<List<Category>> = repository.allCategories.asLiveData()

    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                val newCategory = Category(name = name)
                repository.addCategory(newCategory)
                Log.d(TAG, "Successfully added category: $name")
            } catch (e: Exception) {
                Log.e(TAG, "Error adding category: ${e.message}")
            }
        }
    }

    // --- EXPENSES ---
    fun addExpense(expense: Expense) {
        viewModelScope.launch {
            try {
                repository.addExpense(expense)
                Log.d(TAG, "Expense saved: ${expense.description}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save expense: ${e.message}")
            }
        }
    }

    // RUBRIC: Filter list by period
    fun getExpensesForPeriod(start: Long, end: Long): LiveData<List<Expense>> {
        return repository.getExpensesForPeriod(start, end).asLiveData()
    }

    // --- GOALS ---

    // NEW: Added to allow MainActivity to read the goal from RoomDB
    // This solves the issue of the progress bar not reflecting the actual goal.
    fun getGoalForMonth(monthYear: String): LiveData<Goal?> {
        return repository.getGoalByMonth(monthYear).asLiveData()
    }

    fun updateGoal(monthYear: String, min: Double, max: Double) {
        viewModelScope.launch {
            try {
                val goal = Goal(monthYear = monthYear, minGoal = min, maxGoal = max)
                repository.updateGoal(goal)
                Log.d(TAG, "Goal updated for $monthYear: Max R$max")
            } catch (e: Exception) {
                Log.e(TAG, "Error updating goal: ${e.message}")
            }
        }
    }
}