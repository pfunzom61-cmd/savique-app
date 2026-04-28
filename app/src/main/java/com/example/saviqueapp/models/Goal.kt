package com.example.saviqueapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 *
 * DEFENSIVE: Use default values of 0.0 to prevent calculation crashes.
 */
@Entity(tableName = "goal_table")
data class Goal(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val monthYear: String = "", // Format like "April 2026"

    val minGoal: Double = 0.0,

    val maxGoal: Double = 0.0
)