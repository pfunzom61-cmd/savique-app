package com.example.saviqueapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.saviqueapp.models.*


// We list all our entities here so Room knows which tables to create
@Database(entities = [User::class, Category::class, Expense::class, Goal::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // These link our DAOs to the database
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null


        // This prevents memory leaks and data corruption crashes.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "savique_database"
                )
                    .fallbackToDestructiveMigration() // Scalable: Handles version changes without crashing
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}