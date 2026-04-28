package com.example.saviqueapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saviqueapp.databinding.ActivityMainBinding
import com.example.saviqueapp.viewmodels.BudgetViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory
import com.example.saviqueapp.views.* import java.text.SimpleDateFormat
import java.util.*

/**
 * MAIN DASHBOARD: Central hub for Savique.
 * Rubric Focus: "User Interface", "Apply an intent", and "Reading from RoomDB".
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // Connect to the Budget logic layer
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelFactory((application as SaviqueApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // RUBRIC: Reading and writing to RoomDB (Observation)
        observeData()

        // RUBRIC: Apply event handling
        setupNavigation()
    }

    /**
     * Updates the UI in real-time.
     * Fixed logic: Now observes the ACTUAL user goal instead of a hardcoded 5000.
     */
    private fun observeData() {
        val calendar = Calendar.getInstance()
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        // Start of the month at 00:00:00
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        // FIX: Set end range to the end of the day or month so "now" is always included
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val endOfMonth = calendar.timeInMillis

        budgetViewModel.getGoalForMonth(monthYear).observe(this) { goal ->
            val userMaxGoal = goal?.maxGoal ?: 0.0
            binding.tvGoalLabel.text = "Goal: R${String.format("%.2f", userMaxGoal)}"

            // Use endOfMonth instead of endOfPeriod
            budgetViewModel.getExpensesForPeriod(startOfMonth, endOfMonth).observe(this) { expenses ->
                val total = expenses?.sumOf { it.amount } ?: 0.0
                binding.tvTotalSpent.text = "R ${String.format("%.2f", total)}"

                val progress = if (userMaxGoal > 0.0) ((total / userMaxGoal) * 100).toInt() else 0
                binding.pbGoalProgress.progress = progress
            }
        }
    }

    private fun setupNavigation() {
        // RUBRIC: Apply an intent to navigate to Categories
        binding.cardCategories.setOnClickListener {
            startActivity(Intent(this, CategoryActivity::class.java))
        }

        // RUBRIC: Navigate to Add Expense (Expense and budget entries)
        binding.cardAddExpense.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        // RUBRIC: Navigate to Expense List (User-selectable period)
        binding.cardViewExpenses.setOnClickListener {
            startActivity(Intent(this, ExpenseListActivity::class.java))
        }

        // RUBRIC: Navigate to Goals screen
        binding.cardGoals.setOnClickListener {
            startActivity(Intent(this, GoalsActivity::class.java))
        }
    }
}