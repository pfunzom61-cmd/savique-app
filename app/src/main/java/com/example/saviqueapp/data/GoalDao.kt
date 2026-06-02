package com.example.saviqueapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saviqueapp.models.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setGoal(goal: Goal)

    // DEFENSIVE: Filter by userId so users don't see each other's goals
    @Query("SELECT * FROM goal_table WHERE userId = :userId AND monthYear = :month ORDER BY id DESC LIMIT 1")
    fun getGoalForMonth(userId: String, month: String): Flow<Goal?>
}