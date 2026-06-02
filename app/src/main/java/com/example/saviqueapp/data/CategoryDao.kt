package com.example.saviqueapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.saviqueapp.models.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    // DEFENSIVE: Only return categories belonging to the logged-in user
    @Query("SELECT * FROM category_table WHERE userId = :userId ORDER BY name ASC")
    fun getAllCategories(userId: String): Flow<List<Category>>
}