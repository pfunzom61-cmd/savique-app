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

    // DEFENSIVE: Filter by both date range AND userId
    @Query("SELECT * FROM expense_table WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDate(userId: String, startDate: Long, endDate: Long): Flow<List<Expense>>

    // DEFENSIVE: Filter category totals by userId too
    @Query("SELECT SUM(amount) FROM expense_table WHERE userId = :userId AND categoryId = :catId AND date BETWEEN :start AND :end")
    suspend fun getTotalForCategory(userId: String, catId: Int, start: Long, end: Long): Double?
}