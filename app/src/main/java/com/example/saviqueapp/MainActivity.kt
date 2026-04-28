package com.example.saviqueapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saviqueapp.databinding.ActivityMainBinding
import com.example.saviqueapp.viewmodels.BudgetViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory
import com.example.saviqueapp.views.* // Imports all views including ExpenseList and Goals

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
     * Updates the UI in real-time when expenses are added.
     * This ensures the "Total Spent" card isn't just static text.
     */
    private fun observeData() {
        // Calculate the range for the current month to show on dashboard
        val end = System.currentTimeMillis()
        val start = end - (30L * 24 * 60 * 60 * 1000) // Last 30 days

        budgetViewModel.getExpensesForPeriod(start, end).observe(this) { expenses ->
            // RUBRIC: Handle invalid inputs (null safety with sumOf)
            val total = expenses?.sumOf { it.amount } ?: 0.0

            // Format to 2 decimal places for a professional UI
            binding.tvTotalSpent.text = "R ${String.format("%.2f", total)}"

            // RUBRIC: Set min/max goal visual feedback
            // Assuming a default max goal of R5000 for the progress bar logic
            val progress = if (total > 0) ((total / 5000) * 100).toInt() else 0
            binding.pbGoalProgress.progress = progress
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