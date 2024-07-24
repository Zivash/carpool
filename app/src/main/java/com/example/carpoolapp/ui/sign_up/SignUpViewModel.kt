package com.example.carpoolapp.ui.sign_up

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.carpoolapp.data.models.User
import com.example.carpoolapp.data.repostories.UserRepository
import kotlinx.coroutines.launch

class SignUpViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = UserRepository(application)

    private val _imageUri = MutableLiveData<Uri?>()
    val imageUri: LiveData<Uri?> get() = _imageUri

    fun addUser(user: User) {
        viewModelScope.launch {
            repository.addUser(user)
        }
    }

    fun setImageUri(imageUri: Uri?) {
        _imageUri.value = null
        _imageUri.value = imageUri
    }
}