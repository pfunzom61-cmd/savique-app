package com.example.saviqueapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expense_table")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val description: String = "",

    val amount: Double = 0.0,

    val date: Long = 0L, // Stored as a timestamp for easy sorting/filtering

    val startTime: String = "",

    val endTime: String = "",

    val categoryId: Int = 0,    // This links the expense to a Category ID

    val photoUri: String? = null // Optional: Stores the path to the photo
)