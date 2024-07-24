package com.example.carpoolapp.ui.rides_info

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.carpoolapp.data.models.Ride
import com.example.carpoolapp.databinding.RideLayoutBinding
import java.util.Locale

@Suppress("DEPRECATION")
class RideAdapter(
    private val context: Context,
    private val rides: MutableList<Ride>,
    private val callBack: RideListener
) :
    RecyclerView.Adapter<RideAdapter.RideViewHolder>() {

    interface RideListener {
        fun onItemClick(index: Int)
    }

    inner class RideViewHolder(private val binding: RideLayoutBinding) :
        RecyclerView.ViewHolder(binding.root), OnClickListener {

        init {
            binding.root.setOnClickListener(this)
        }

        fun bind(ride: Ride) {
            val price = StringBuilder(ride.price.toString() + " $")
            val origin = convertLatLongToAddress(ride.originLat!!, ride.originLng!!)
            val destination = convertLatLongToAddress(ride.destinationLat!!, ride.destinationLng!!)

            binding.tvOriginAddress.text = origin
            binding.tvDestinationAddress.text = destination
            binding.tvDate.text = ride.date
            binding.tvTime.text = ride.time
            binding.tvSeats.text = ride.availableSeats.toString()
            binding.tvPrice.text = price
        }

        override fun onClick(p0: View?) {
            callBack.onItemClick(adapterPosition)
        }
    }

    override fun onBindViewHolder(holder: RideViewHolder, position: Int) =
        holder.bind(rides[position])

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        RideViewHolder(
            RideLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount() = rides.size

    fun itemAt(index: Int) = rides[index]

    private fun convertLatLongToAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)!!
        if (addresses.isNotEmpty()) {
            val address: Address = addresses[0]
            val addressLines =
                (0..address.maxAddressLineIndex).map { address.getAddressLine(it) }
            return addressLines.joinToString(separator = ", ")
        }

        return ""
    }
}