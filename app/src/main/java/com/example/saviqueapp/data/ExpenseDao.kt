package com.example.saviqueapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.saviqueapp.models.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insertExpense(expense: Expense)

    // RUBRIC: View list of entries during a user-selectable period
    // We use Long (timestamps) to make date comparison fast and crash-proof
    @Query("SELECT * FROM expense_table WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDate(startDate: Long, endDate: Long): Flow<List<Expense>>

    // RUBRIC: View total amount spent on each category during a period

    @Query("SELECT SUM(amount) FROM expense_table WHERE categoryId = :catId AND date BETWEEN :start AND :end")
    suspend fun getTotalForCategory(catId: Int, start: Long, end: Long): Double?
}