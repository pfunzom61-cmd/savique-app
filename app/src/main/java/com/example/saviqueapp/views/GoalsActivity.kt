package com.example.saviqueapp.views

import android.os.Bundle
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

    private lateinit var binding: ActivityGoalsBinding
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelFactory((application as SaviqueApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGoalsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSaveGoal.setOnClickListener {
            val maxAmount = binding.etMaxGoal.text.toString().toDoubleOrNull() ?: 0.0

            if (maxAmount > 0) {
                // Formatting date like "April 2026" to match your Goal model logic
                val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

                budgetViewModel.updateGoal(monthYear, 0.0, maxAmount)
                Toast.makeText(this, "Monthly goal updated!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            }
        }
    }
}