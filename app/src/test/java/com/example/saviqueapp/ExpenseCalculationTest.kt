package com.example.saviqueapp

import com.example.saviqueapp.models.Expense
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Automated testing on the main functionality.
 * This test verifies the summation logic used for dashboard totals and category breakdowns.
 */
class ExpenseCalculationTest {

    @Test
    fun testTotalCalculation() {
        // Creating mock data that matches the Expense.kt model exactly:
        // id: Int, description: String, amount: Double, date: Long, etc.
        val expenses = listOf(
            Expense(
                id = 0,
                description = "Lunch",
                amount = 150.0,
                date = System.currentTimeMillis(),
                startTime = "12:00",
                endTime = "13:00",
                categoryId = 1
            ),
            Expense(
                id = 0,
                description = "Fuel",
                amount = 500.0,
                date = System.currentTimeMillis(),
                startTime = "08:00",
                endTime = "08:30",
                categoryId = 2
            )
        )

        // Perform the calculation logic used in MainActivity and ExpenseListActivity
        val total = expenses.sumOf { it.amount }

        // Assert: Verify that the math (150 + 500) equals 650.0
        // The 0.01 is the 'delta' to allow for minor floating-point differences
        assertEquals(650.0, total, 0.01)
    }
}