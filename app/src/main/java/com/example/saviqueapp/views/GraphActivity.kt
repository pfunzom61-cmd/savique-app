package com.example.saviqueapp.views

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.saviqueapp.SaviqueApplication
import com.example.saviqueapp.databinding.ActivityGraphBinding
import com.example.saviqueapp.viewmodels.BudgetViewModel
import com.example.saviqueapp.viewmodels.ViewModelFactory
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class GraphActivity : AppCompatActivity() {

    private val TAG = "SAVIQUE_GRAPH"
    private lateinit var binding: ActivityGraphBinding
    private val budgetViewModel: BudgetViewModel by viewModels {
        ViewModelFactory((application as SaviqueApplication).repository)
    }

    // Track current period for re-drawing when goal loads
    private var currentStart = 0L
    private var currentEnd = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGraphBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "GraphActivity created")

        // Default: last 30 days
        currentEnd = System.currentTimeMillis()
        currentStart = currentEnd - (30L * 24 * 60 * 60 * 1000)

        loadGraph(currentStart, currentEnd)

        binding.btnFilterGraph.setOnClickListener {
            showDateRangePicker()
        }
    }

    private fun loadGraph(start: Long, end: Long) {
        currentStart = start
        currentEnd = end

        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        binding.tvGraphPeriod.text =
            "Showing: ${sdf.format(Date(start))} — ${sdf.format(Date(end))}"

        // Get current month for goal lookup
        val monthYear = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            .format(Calendar.getInstance().time)

        // Observe expenses for the selected period
        budgetViewModel.getExpensesForPeriod(start, end).observe(this) { expenses ->
            if (expenses.isEmpty()) {
                Log.d(TAG, "No expenses found for period")
                binding.barChart.clear()
                binding.barChart.setNoDataText("No expenses for this period")
                return@observe
            }

            // Group expenses by categoryId and sum amounts
            val totalsMap = expenses
                .groupBy { it.categoryId }
                .mapValues { entry -> entry.value.sumOf { it.amount } }

            Log.d(TAG, "Category totals: $totalsMap")

            // Observe categories to get names for the X axis
            budgetViewModel.allCategories.observe(this) { categories ->
                val entries = mutableListOf<BarEntry>()
                val labels = mutableListOf<String>()

                // Only include categories that have spending in this period
                val activeCategories = categories.filter { totalsMap.containsKey(it.id) }

                activeCategories.forEachIndexed { index, category ->
                    val total = totalsMap[category.id]?.toFloat() ?: 0f
                    entries.add(BarEntry(index.toFloat(), total))
                    labels.add(category.name)
                    Log.d(TAG, "Bar ${category.name}: R$total")
                }

                if (entries.isEmpty()) {
                    binding.barChart.clear()
                    binding.barChart.setNoDataText("No category data available")
                    return@observe
                }

                val dataSet = BarDataSet(entries, "Spending per Category").apply {
                    color = android.graphics.Color.parseColor("#26A69A")
                    valueTextSize = 12f
                }

                val barData = BarData(dataSet).apply {
                    barWidth = 0.5f
                }

                // Observe goal to draw min/max lines on the chart
                budgetViewModel.getGoalForMonth(monthYear).observe(this) { goal ->
                    binding.barChart.apply {
                        data = barData

                        // X axis: show category names
                        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        xAxis.granularity = 1f
                        xAxis.setDrawGridLines(false)

                        // Clean up chart appearance
                        axisRight.isEnabled = false
                        description.isEnabled = false
                        legend.isEnabled = true

                        // RUBRIC: Show min and max goal lines
                        axisLeft.removeAllLimitLines()

                        if (goal != null && goal.maxGoal > 0.0) {
                            val maxLine = LimitLine(
                                goal.maxGoal.toFloat(), "Max Goal"
                            ).apply {
                                lineColor = android.graphics.Color.parseColor("#FF5252")
                                lineWidth = 2f
                                textColor = android.graphics.Color.parseColor("#FF5252")
                                textSize = 11f
                            }
                            axisLeft.addLimitLine(maxLine)
                            Log.d(TAG, "Max goal line drawn at ${goal.maxGoal}")
                        }

                        if (goal != null && goal.minGoal > 0.0) {
                            val minLine = LimitLine(
                                goal.minGoal.toFloat(), "Min Goal"
                            ).apply {
                                lineColor = android.graphics.Color.parseColor("#26A69A")
                                lineWidth = 2f
                                textColor = android.graphics.Color.parseColor("#26A69A")
                                textSize = 11f
                            }
                            axisLeft.addLimitLine(minLine)
                            Log.d(TAG, "Min goal line drawn at ${goal.minGoal}")
                        }

                        animateY(800)
                        invalidate()
                    }
                }
            }
        }
    }

    private fun showDateRangePicker() {
        val cal = Calendar.getInstance()
        // First picker: select START date
        DatePickerDialog(this, { _, y, m, d ->
            cal.set(y, m, d, 0, 0, 0)
            val start = cal.timeInMillis

            // Second picker: select END date
            DatePickerDialog(this, { _, y2, m2, d2 ->
                cal.set(y2, m2, d2, 23, 59, 59)
                val end = cal.timeInMillis
                Log.d(TAG, "Date range selected: $start to $end")
                loadGraph(start, end)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()

        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
    }
}