package com.example.saviqueapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_table")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val monthYear: String = "",
    val minGoal: Double = 0.0,
    val maxGoal: Double = 0.0,
    val userId: String = "" // Isolates data per Firebase user
)