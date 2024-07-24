package com.example.carpoolapp.data.models

data class Ride(
    var originLat: Double? = null,
    var originLng: Double? = null,
    var destinationLat: Double? = null,
    var destinationLng: Double? = null,
    var date: String? = null,
    var time: String? = null,
    var availableSeats: Int? = null,
    var price: Int? = null,
    var driverId: String? = null,
) {

    constructor() : this(null, null, null, null, null, null, null, null, null)
}
