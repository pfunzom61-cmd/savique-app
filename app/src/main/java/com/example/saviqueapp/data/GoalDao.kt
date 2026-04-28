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

    // Changed: Removed 'suspend' and added 'Flow' so the Dashboard updates automatically
    @Query("SELECT * FROM goal_table WHERE monthYear = :month ORDER BY id DESC LIMIT 1")
    fun getGoalForMonth(month: String): kotlinx.coroutines.flow.Flow<Goal?>
}