package com.example.saviqueapp.views

import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.saviqueapp.SaviqueApplication
import com.example.saviqueapp.databinding.ActivityExpenseListBinding
import com.example.saviqueapp.models.Expense
import com.example.saviqueapp.viewmodels.BudgetViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ExpenseListActivity : AppCompatActivity() {

    private val TAG = "SAVIQUE_HISTORY"
    private lateinit var binding: ActivityExpenseListBinding
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelFactory((application as SaviqueApplication).repository)
    }

    // Track current expenses for CSV export
    private var currentExpenses: List<Expense> = emptyList()
    private var currentStart: Long = 0L
    private var currentEnd: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpenseListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "History Activity Created")

        val adapter = ExpenseAdapter()
        binding.rvExpenses.layoutManager = LinearLayoutManager(this)
        binding.rvExpenses.adapter = adapter

        // Initial load: Last 30 days
        currentEnd = System.currentTimeMillis()
        currentStart = currentEnd - (30L * 24 * 60 * 60 * 1000)

        loadExpenses(currentStart, currentEnd, adapter)

        binding.btnFilter.setOnClickListener {
            showDateRangePicker(adapter)
        }

        // CUSTOM FEATURE 2: CSV Export
        binding.btnExportCsv.setOnClickListener {
            if (currentExpenses.isEmpty()) {
                Toast.makeText(this, "No expenses to export", Toast.LENGTH_SHORT).show()
            } else {
                exportToCsv()
            }
        }

        // CUSTOM FEATURE 2: Save directly to Downloads folder
        binding.btnSaveDownloads.setOnClickListener {
            if (currentExpenses.isEmpty()) {
                Toast.makeText(this, "No expenses to save", Toast.LENGTH_SHORT).show()
            } else {
                saveToDownloads()
            }
        }
    }

    private fun loadExpenses(start: Long, end: Long, adapter: ExpenseAdapter) {
        Log.d(TAG, "Loading expenses from $start to $end")

        budgetViewModel.getExpensesForPeriod(start, end).observe(this) { expenses ->
            currentExpenses = expenses // Store for CSV export
            adapter.submitList(expenses)
            calculateCategoryTotals(expenses)
        }
    }

    private fun calculateCategoryTotals(expenses: List<Expense>) {
        if (expenses.isEmpty()) {
            binding.tvCategorySummary.text = "No expenses found for this period."
            return
        }

        val totalsMap = expenses.groupBy { it.categoryId }
            .mapValues { it.value.sumOf { exp -> exp.amount } }

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
            currentStart = start
            currentEnd = end
            loadExpenses(start, end, adapter)
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }

    /**
     * CUSTOM FEATURE 2: CSV Export
     * Writes current expenses to a .csv file and opens Android share sheet.
     * User can save to Downloads, email it, or share via any app.
     */
    private fun exportToCsv() {
        Log.d(TAG, "Starting CSV export for ${currentExpenses.size} expenses")

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fileName = "savique_expenses_${sdf.format(Date())}.csv"
            val file = File(filesDir, fileName)
            val writer = FileWriter(file)

            // CSV Header row
            writer.append("Description,Amount,Date,Start Time,End Time,Category ID\n")

            // Write each expense as a row
            currentExpenses.forEach { expense ->
                val dateStr = sdf.format(Date(expense.date))
                // Wrap description in quotes to handle commas inside descriptions
                writer.append("\"${expense.description}\",")
                writer.append("${expense.amount},")
                writer.append("$dateStr,")
                writer.append("${expense.startTime},")
                writer.append("${expense.endTime},")
                writer.append("${expense.categoryId}\n")
            }

            writer.flush()
            writer.close()

            Log.d(TAG, "CSV written to ${file.absolutePath}")

            // Share the file via Android share sheet
            val uri: Uri = FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Savique Expense Export")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Export expenses via..."))
            Log.d(TAG, "CSV share intent launched")

        } catch (e: Exception) {
            Log.e(TAG, "CSV export failed: ${e.message}")
            Toast.makeText(this, "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /**
     * CUSTOM FEATURE 2: Save CSV directly to device Downloads folder.
     * Works on emulator and real device without needing Drive or email.
     */
    private fun saveToDownloads() {
        Log.d(TAG, "Saving CSV to Downloads folder")
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fileName = "savique_expenses_${sdf.format(Date())}.csv"

            // Save to public Downloads folder — visible in Device Explorer
            val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_DOWNLOADS
            )
            val file = File(downloadsDir, fileName)
            val writer = FileWriter(file)

            // CSV Header
            writer.append("Description,Amount,Date,Start Time,End Time,Category ID\n")

            // Write each expense
            currentExpenses.forEach { expense ->
                val dateStr = sdf.format(Date(expense.date))
                writer.append("\"${expense.description}\",")
                writer.append("${expense.amount},")
                writer.append("$dateStr,")
                writer.append("${expense.startTime},")
                writer.append("${expense.endTime},")
                writer.append("${expense.categoryId}\n")
            }

            writer.flush()
            writer.close()

            Log.d(TAG, "CSV saved to Downloads: ${file.absolutePath}")
            Toast.makeText(
                this,
                "✅ Saved to Downloads: $fileName",
                Toast.LENGTH_LONG
            ).show()

        } catch (e: Exception) {
            Log.e(TAG, "Save to Downloads failed: ${e.message}")
            Toast.makeText(this, "Save failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}