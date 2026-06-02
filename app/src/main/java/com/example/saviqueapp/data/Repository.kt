package com.example.saviqueapp.data

import android.util.Log
import com.example.saviqueapp.models.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class Repository(
    private val userDao: UserDao,
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val goalDao: GoalDao
) {
    private val TAG = "SAVIQUE_REPO"
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // DEFENSIVE: Returns empty string if no user logged in — queries will return empty results
    private fun getUid(): String = auth.currentUser?.uid ?: ""

    // --- USER LOGIC ---
    suspend fun login(u: String, p: String) = userDao.login(u, p)
    suspend fun register(user: User) = userDao.insertUser(user)

    // --- CATEGORY LOGIC ---
    // Now filtered by userId automatically
    val allCategories: Flow<List<Category>>
        get() {
            val uid = getUid()
            if (uid.isEmpty()) {
                Log.w(TAG, "No user logged in — returning empty categories")
                return emptyFlow()
            }
            return categoryDao.getAllCategories(uid)
        }

    suspend fun addCategory(category: Category) {
        val uid = getUid()
        if (uid.isEmpty()) {
            Log.e(TAG, "Cannot add category — no user logged in")
            return
        }
        val categoryWithUser = category.copy(userId = uid)
        categoryDao.insertCategory(categoryWithUser)
        Log.d(TAG, "Category saved to Room: ${category.name}")

        val data = hashMapOf(
            "name" to categoryWithUser.name,
            "iconName" to categoryWithUser.iconName,
            "userId" to uid
        )
        firestore.collection("users").document(uid)
            .collection("categories").document(category.name)
            .set(data)
            .addOnSuccessListener { Log.d(TAG, "Category synced to Firestore: ${category.name}") }
            .addOnFailureListener { e -> Log.e(TAG, "Firestore category sync failed: ${e.message}") }
    }

    // --- EXPENSE LOGIC ---
    suspend fun addExpense(expense: Expense) {
        val uid = getUid()
        if (uid.isEmpty()) {
            Log.e(TAG, "Cannot add expense — no user logged in")
            return
        }
        val expenseWithUser = expense.copy(userId = uid)
        expenseDao.insertExpense(expenseWithUser)
        Log.d(TAG, "Expense saved to Room: ${expense.description}")

        val data = hashMapOf(
            "description" to expenseWithUser.description,
            "amount" to expenseWithUser.amount,
            "date" to expenseWithUser.date,
            "startTime" to expenseWithUser.startTime,
            "endTime" to expenseWithUser.endTime,
            "categoryId" to expenseWithUser.categoryId,
            "photoUri" to (expenseWithUser.photoUri ?: ""),
            "userId" to uid
        )
        firestore.collection("users").document(uid)
            .collection("expenses").add(data)
            .addOnSuccessListener { Log.d(TAG, "Expense synced to Firestore: ${expense.description}") }
            .addOnFailureListener { e -> Log.e(TAG, "Firestore expense sync failed: ${e.message}") }
    }

    fun getExpensesForPeriod(start: Long, end: Long): Flow<List<Expense>> {
        val uid = getUid()
        if (uid.isEmpty()) {
            Log.w(TAG, "No user logged in — returning empty expenses")
            return emptyFlow()
        }
        return expenseDao.getExpensesByDate(uid, start, end)
    }

    suspend fun getCategoryTotal(id: Int, start: Long, end: Long): Double? {
        val uid = getUid()
        if (uid.isEmpty()) return null
        return expenseDao.getTotalForCategory(uid, id, start, end)
    }

    // --- GOAL LOGIC ---
    suspend fun updateGoal(goal: Goal) {
        val uid = getUid()
        if (uid.isEmpty()) {
            Log.e(TAG, "Cannot update goal — no user logged in")
            return
        }
        val goalWithUser = goal.copy(userId = uid)
        goalDao.setGoal(goalWithUser)
        Log.d(TAG, "Goal saved to Room: ${goal.monthYear}")

        val data = hashMapOf(
            "monthYear" to goalWithUser.monthYear,
            "minGoal" to goalWithUser.minGoal,
            "maxGoal" to goalWithUser.maxGoal,
            "userId" to uid
        )
        firestore.collection("users").document(uid)
            .collection("goals").document(goal.monthYear)
            .set(data)
            .addOnSuccessListener { Log.d(TAG, "Goal synced to Firestore: ${goal.monthYear}") }
            .addOnFailureListener { e -> Log.e(TAG, "Firestore goal sync failed: ${e.message}") }
    }

    fun getGoalByMonth(month: String): Flow<Goal?> {
        val uid = getUid()
        if (uid.isEmpty()) {
            Log.w(TAG, "No user logged in — returning empty goal")
            return emptyFlow()
        }
        return goalDao.getGoalForMonth(uid, month)
    }
}