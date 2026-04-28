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

    @Query("SELECT * FROM category_table ORDER BY name ASC")
    fun getAllCategories(): Flow<List<Category>>
}