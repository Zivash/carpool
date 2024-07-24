package com.example.carpoolapp.ui.add_ride

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class AddRideViewModel : ViewModel() {

    private val _originName = MutableLiveData<String?>()
    val originName: LiveData<String?> get() = _originName

    private val _originLatLng = MutableLiveData<LatLng?>()
    val originLatLng: LiveData<LatLng?> get() = _originLatLng

    private val _destinationName = MutableLiveData<String?>()
    val destinationName: LiveData<String?> get() = _destinationName

    private val _destinationLatLng = MutableLiveData<LatLng?>()
    val destinationLatLng: LiveData<LatLng?> get() = _destinationLatLng

    private val _date = MutableLiveData<String?>()
    val date: LiveData<String?> get() = _date

    private val _time = MutableLiveData<String?>()
    val time: LiveData<String?> get() = _time

    private val _seats = MutableLiveData<Int?>()
    val seats: LiveData<Int?> get() = _seats

    fun setDate(date: String?) {
        _date.value = date
    }

    fun setTime(time: String?) {
        _time.value = time
    }

    fun setOriginName(originName: String?) {
        _originName.value = originName
    }

    fun setDestinationName(destinationName: String?) {
        _destinationName.value = destinationName
    }

    fun setOriginLatLng(originLatLng: LatLng?) {
        _originLatLng.value = originLatLng
    }

    fun setDestinationLatLng(destinationLatLng: LatLng?) {
        _destinationLatLng.value = destinationLatLng
    }

    fun setSeats(seats: Int?) {
        _seats.value = seats
    }
}