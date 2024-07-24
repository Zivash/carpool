package com.example.carpoolapp.data.local_db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.carpoolapp.data.models.User

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("DELETE FROM user_details")
    suspend fun clearAll()

    @Query("SELECT * FROM user_details")
    suspend fun getUserDetails(): User?
}