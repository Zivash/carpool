package com.example.carpoolapp.ui.rides_info

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.carpoolapp.data.models.Ride
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class RideDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val _rides = MutableLiveData<MutableList<Ride>>()
    val rides: LiveData<MutableList<Ride>> get() = _rides

    private val _ridesId = MutableLiveData<MutableList<String>>()

    private val _name = MutableLiveData<String?>()
    val name: LiveData<String?> get() = _name

    private val _phone = MutableLiveData<String?>()
    val phone: LiveData<String?> get() = _phone

    private val _picture = MutableLiveData<String?>()
    val picture: LiveData<String?> get() = _picture

    init {
        _rides.value = mutableListOf()
        _ridesId.value = mutableListOf()
    }

    fun setLists(list: MutableList<Ride>, listId: MutableList<String>) {
        _rides.value = list
        _ridesId.value = listId
    }

    fun getListId(index: Int): String {
        return _ridesId.value?.get(index) ?: ""
    }

    fun setName(name: String?) {
        _name.value = name
    }

    fun setPhone(phone: String?) {
        _phone.value = phone
    }

    fun deleteRide(ride: Ride, rideId: String) {
        _rides.value?.remove(ride)
        _ridesId.value?.remove(rideId)
    }

    suspend fun setPicture(picture: String?) {
        var imageRef: StorageReference?

        CoroutineScope(Dispatchers.IO).launch {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.reference
            imageRef = storageRef.child(picture!!)

            val uri = imageRef?.downloadUrl?.await()

            withContext(Dispatchers.Main) {
                _picture.value = uri.toString()
            }

        }
    }
}