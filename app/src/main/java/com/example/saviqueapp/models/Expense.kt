package com.example.saviqueapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val description: String = "",
    val amount: Double = 0.0,
    val date: Long = 0L,
    val startTime: String = "",
    val endTime: String = "",
    val categoryId: Int = 0,
    val photoUri: String? = null,
    val userId: String = "" // Isolates data per Firebase user
)