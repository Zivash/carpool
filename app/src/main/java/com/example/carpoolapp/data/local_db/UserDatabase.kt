package com.example.carpoolapp.data.local_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.carpoolapp.data.models.User

@Database(entities = [User::class], version = 3, exportSchema = false)
abstract class UserDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var instance: UserDatabase? = null

        fun getDatabase(context: Context) = instance ?: synchronized(UserDatabase::class.java) {

            Room.databaseBuilder(context.applicationContext, UserDatabase::class.java, "user_db")
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
        }
    }
}