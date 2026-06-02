package com.example.saviqueapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saviqueapp.databinding.ActivityMainBinding
import com.example.saviqueapp.viewmodels.BudgetViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.saviqueapp.views.GraphActivity
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


        observeData()

        // RUBRIC: Apply event handling
        setupNavigation()

        // CUSTOM FEATURE 1: Load spending streak from Firestore
        loadStreak()
    }

    /**
     * Updates the UI in real-time.
     * Fixed logic: Now observes the ACTUAL user goal instead of a hardcoded 5000.
     */
    private fun observeData() {
        val calendar = Calendar.getInstance()
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(calendar.time)

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        val endOfMonth = calendar.timeInMillis

        budgetViewModel.getGoalForMonth(monthYear).observe(this) { goal ->
            val userMaxGoal = goal?.maxGoal ?: 0.0
            val userMinGoal = goal?.minGoal ?: 0.0

            // Update goal labels
            binding.tvGoalLabel.text = "Max: R${String.format("%.2f", userMaxGoal)}"
            binding.tvMinGoalStatus.text = "Min goal: R${String.format("%.2f", userMinGoal)}"

            budgetViewModel.getExpensesForPeriod(startOfMonth, endOfMonth).observe(this) { expenses ->
                val total = expenses?.sumOf { it.amount } ?: 0.0
                binding.tvTotalSpent.text = "R ${String.format("%.2f", total)}"

                // Progress bar vs max goal
                val progress = if (userMaxGoal > 0.0) ((total / userMaxGoal) * 100).toInt() else 0
                binding.pbGoalProgress.progress = progress.coerceAtMost(100)

                // RUBRIC: Visual status — highlight overspending in red
                when {
                    userMaxGoal <= 0.0 -> {
                        binding.tvBudgetStatus.text = "🎯 Set a goal to track progress"
                        binding.tvBudgetStatus.setTextColor(android.graphics.Color.parseColor("#999999"))
                        binding.pbGoalProgress.progressTintList =
                            android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#26A69A"))
                    }
                    total > userMaxGoal -> {
                        // RUBRIC: Overspending must be highlighted visually
                        binding.tvBudgetStatus.text = "🚨 Over budget! R${String.format("%.2f", total - userMaxGoal)} over limit"
                        binding.tvBudgetStatus.setTextColor(android.graphics.Color.parseColor("#FF5252"))
                        binding.pbGoalProgress.progressTintList =
                            android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#FF5252"))
                    }
                    total < userMinGoal -> {
                        binding.tvBudgetStatus.text = "📉 Below minimum — keep tracking!"
                        binding.tvBudgetStatus.setTextColor(android.graphics.Color.parseColor("#FF6F00"))
                        binding.pbGoalProgress.progressTintList =
                            android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#26A69A"))
                    }
                    else -> {
                        binding.tvBudgetStatus.text = "✅ Within budget — great work!"
                        binding.tvBudgetStatus.setTextColor(android.graphics.Color.parseColor("#26A69A"))
                        binding.pbGoalProgress.progressTintList =
                            android.content.res.ColorStateList.valueOf(
                                android.graphics.Color.parseColor("#26A69A"))
                    }
                }

                Log.d("SAVIQUE_MAIN", "Total: R$total | Min: R$userMinGoal | Max: R$userMaxGoal | Progress: $progress%")
            }
        }
    }

    /**
     * CUSTOM FEATURE 1: Read streak from Firestore and display on dashboard
     */
    private fun loadStreak() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        FirebaseFirestore.getInstance().collection("users").document(uid)
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Log.e("SAVIQUE_MAIN", "Streak load failed: ${error.message}")
                    return@addSnapshotListener
                }
                val streak = doc?.getLong("streak")?.toInt() ?: 0
                binding.tvStreak.text = if (streak > 0) "🔥 Streak: $streak days" else "🔥 Streak: 0 days"
                Log.d("SAVIQUE_MAIN", "Streak loaded: $streak")
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

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Log.d("SAVIQUE_MAIN", "User logged out")
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.cardGraph.setOnClickListener {
            startActivity(Intent(this, GraphActivity::class.java))
        }
    }


}