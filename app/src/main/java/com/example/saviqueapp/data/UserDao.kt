package com.example.saviqueapp.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.saviqueapp.models.User

@Dao
interface UserDao {
    //  OnConflictStrategy.REPLACE ensures if a user exists, we don't crash
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    // Log in logic
    @Query("SELECT * FROM user_table WHERE username = :uname AND password = :pword LIMIT 1")
    suspend fun login(uname: String, pword: String): User?
}