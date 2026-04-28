package com.example.saviqueapp.data

import com.example.saviqueapp.models.*
import kotlinx.coroutines.flow.Flow

/**
 * THE MANAGER: This class abstracts access to multiple DAOs.
 * It provides a clean API for the rest of the app to interact with data.
 */
class Repository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val goalDao: GoalDao
) {
    // --- USER LOGIC ---
    suspend fun login(u: String, p: String) = userDao.login(u, p)
    suspend fun register(user: User) = userDao.insertUser(user)

    // --- CATEGORY LOGIC ---
    val allCategories: Flow<List<Category>> = categoryDao.getAllCategories()
    suspend fun addCategory(category: Category) = categoryDao.insertCategory(category)

    // --- EXPENSE LOGIC ---
    suspend fun addExpense(expense: Expense) = expenseDao.insertExpense(expense)

    // RUBRIC: Filter by user-selected period
    fun getExpensesForPeriod(start: Long, end: Long) = expenseDao.getExpensesByDate(start, end)

    // RUBRIC: Category totals for period
    suspend fun getCategoryTotal(id: Int, start: Long, end: Long) = expenseDao.getTotalForCategory(id, start, end)

    // --- GOAL LOGIC ---
    suspend fun updateGoal(goal: Goal) = goalDao.setGoal(goal)

    // Fix: Change this to return the Flow from the GoalDao
    fun getGoalByMonth(month: String): kotlinx.coroutines.flow.Flow<Goal?> {
        return goalDao.getGoalForMonth(month)
    }
}