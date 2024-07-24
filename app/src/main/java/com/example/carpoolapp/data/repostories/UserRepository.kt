package com.example.carpoolapp.data.repostories

import android.app.Application
import com.example.carpoolapp.data.local_db.UserDao
import com.example.carpoolapp.data.local_db.UserDatabase
import com.example.carpoolapp.data.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(application: Application) {
    private var userDao: UserDao?

    init {
        val db = UserDatabase.getDatabase(application.applicationContext)
        userDao = db.userDao()
    }

    suspend fun getUser(): User? {
        return withContext(Dispatchers.IO) {
            userDao?.getUserDetails()
        }
    }

    suspend fun addUser(user: User) {
        userDao?.clearAll()
        userDao?.insert(user)
    }
}