package com.example.saviqueapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saviqueapp.models.Goal

@Dao
interface GoalDao {
    // We use REPLACE so if they update their goal for the month, it just overwrites the old one
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGoal(goal: Goal)

    @Query("SELECT * FROM goal_table WHERE monthYear = :month LIMIT 1")
    suspend fun getGoalForMonth(month: String): Goal?
}