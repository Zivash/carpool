package com.example.carpoolapp.ui.log_in

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.carpoolapp.data.models.User
import com.example.carpoolapp.data.repostories.UserRepository

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    suspend fun getUser(): User? = repository.getUser()
}