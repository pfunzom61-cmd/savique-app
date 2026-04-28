package com.example.saviqueapp

import android.app.Application
import com.example.saviqueapp.data.AppDatabase
import com.example.saviqueapp.data.Repository

/**
 * THE FOUNDATION: This class runs before the first screen starts.
 * It initializes our Database and Repository once so they can be reused.
 */
class SaviqueApplication : Application() {

    // We use "by lazy" so the database is only created when it's actually needed
    val database by lazy { AppDatabase.getDatabase(this) }

    val repository by lazy {
        Repository(
            database.userDao(),
            database.categoryDao(),
            database.expenseDao(),
            database.goalDao()
        )
    }
}