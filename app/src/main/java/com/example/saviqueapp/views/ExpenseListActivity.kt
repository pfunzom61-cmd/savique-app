package com.example.saviqueapp.views

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saviqueapp.SaviqueApplication
import com.example.saviqueapp.databinding.ActivityExpenseListBinding
import com.example.saviqueapp.models.Expense
import com.example.saviqueapp.viewmodels.BudgetViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory
import java.util.*

class ExpenseListActivity : AppCompatActivity() {

    private val TAG = "SAVIQUE_HISTORY"
    private lateinit var binding: ActivityExpenseListBinding
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelFactory((application as SaviqueApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "History Activity Created")

        val adapter = ExpenseAdapter()
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter

        // Initial load: Last 30 days
        val end = System.currentTimeMillis()
        val start = end - (30L * 24 * 60 * 60 * 1000)

        loadExpenses(start, end, adapter)

        binding.btnFilter.setOnClickListener {
            showDateRangePicker(adapter)
        }
    }

    private fun loadExpenses(start: Long, end: Long, adapter: ExpenseAdapter) {
        Log.d(TAG, "Loading expenses from $start to $end")

        budgetViewModel.getExpensesForPeriod(start, end).observe(this) { expenses ->
            adapter.submitList(expenses)

            // RUBRIC: View category totals in a period
            calculateCategoryTotals(expenses)
        }
    }

    private fun calculateCategoryTotals(expenses: List<Expense>) {
        if (expenses.isEmpty()) {
            binding.tvCategorySummary.text = "No expenses found for this period."
            return
        }

        // Group by categoryId and sum the amounts
        val totalsMap = expenses.groupBy { it.categoryId }
            .mapValues { it.value.sumOf { exp -> exp.amount } }

        // Fetch category names to display a readable summary
        budgetViewModel.allCategories.observe(this) { categories ->
            val summary = StringBuilder()
            categories.forEach { category ->
                val total = totalsMap[category.id] ?: 0.0
                if (total > 0) {
                    summary.append("• ${category.name}: R${String.format("%.2f", total)}\n")
                }
            }
            binding.tvCategorySummary.text = summary.toString()
            Log.d(TAG, "Category Breakdown Updated: ${totalsMap.size} categories active")
        }
    }

    private fun showDateRangePicker(adapter: ExpenseAdapter) {
        val cal = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d, 0, 0)
            val start = cal.timeInMillis
            val end = System.currentTimeMillis()
            loadExpenses(start, end, adapter)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
}