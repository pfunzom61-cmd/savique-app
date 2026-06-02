package com.example.saviqueapp.views

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saviqueapp.SaviqueApplication
import com.example.saviqueapp.databinding.ActivityGoalsBinding
import com.example.saviqueapp.viewmodels.BudgetViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class GoalsActivity : AppCompatActivity() {

    private val TAG = "SAVIQUE_GOALS"
    private lateinit var binding: ActivityGoalsBinding
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelFactory((application as SaviqueApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Pre-fill existing goal values so user can see what they set before
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())
        budgetViewModel.getGoalForMonth(monthYear).observe(this) { goal ->
            if (goal != null) {
                if (goal.minGoal > 0.0) binding.etMinGoal.setText(goal.minGoal.toString())
                if (goal.maxGoal > 0.0) binding.etMaxGoal.setText(goal.maxGoal.toString())
            }
        }

        binding.btnSaveGoal.setOnClickListener {
            val minAmount = binding.etMinGoal.text.toString().toDoubleOrNull() ?: 0.0
            val maxAmount = binding.etMaxGoal.text.toString().toDoubleOrNull() ?: 0.0

            // DEFENSIVE: Validate that max is greater than min
            if (maxAmount <= 0) {
                Toast.makeText(this, "Please enter a valid maximum amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (minAmount >= maxAmount) {
                Toast.makeText(this, "Minimum must be less than maximum", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            budgetViewModel.updateGoal(monthYear, minAmount, maxAmount)
            Log.d(TAG, "Goal saved — Min: R$minAmount Max: R$maxAmount")
            Toast.makeText(this, "Monthly goal updated!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}