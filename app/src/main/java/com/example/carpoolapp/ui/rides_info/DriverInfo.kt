package com.example.carpoolapp.ui.rides_info

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.carpoolapp.R
import com.example.carpoolapp.databinding.DriverInfoBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DriverInfo : Fragment() {

    private var _binding: DriverInfoBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: RideDetailsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DriverInfoBinding.inflate(inflater, container, false)
        viewModel.name.observe(viewLifecycleOwner) {
            binding.driverName.text = it
        }

        viewModel.phone.observe(viewLifecycleOwner) {
            binding.tvPhone.text = it
        }

        viewModel.picture.observe(viewLifecycleOwner) {
            viewModel.picture.value?.let { loadImageIntoImageView() }
        }

        binding.btnCall.setOnClickListener {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${viewModel.phone.value}")
            }

            startActivity(intent)
        }

        if (arguments?.getString("from") == "Find") {
            binding.btnJoinRide.setOnClickListener {
                val seatsNeeded = arguments?.getInt("seats")
                val rideId = arguments?.getString("ride_id")
                val userId = arguments?.getString("user_id")

                val database = FirebaseDatabase.getInstance()
                val allRidesReference = rideId?.let { it1 ->
                    database.getReference("All Rides").child(
                        it1
                    )
                }

                val databaseRef = database.getReference("Users").child(userId!!).child("Passenger")
                    .child(rideId!!)

                databaseRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val dataSnapshot = task.result
                        val currentSeats = dataSnapshot?.getValue(Int::class.java) ?: 0
                        databaseRef.setValue(currentSeats + seatsNeeded!!)
                    }
                }

                allRidesReference?.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if (dataSnapshot.exists()) {
                            val prevSeats =
                                dataSnapshot.child("availableSeats").getValue(Int::class.java)
                            val updates = mapOf(
                                "availableSeats" to (prevSeats!! - seatsNeeded!!)
                            )
                            allRidesReference.updateChildren(updates)

                            Toast.makeText(
                                requireContext(),
                                getString(R.string.joined_ride),
                                Toast.LENGTH_SHORT
                            )
                                .show()

                            findNavController().navigate(
                                R.id.action_driverInfo_to_findFragment,
                                bundleOf("user_id" to userId)
                            )
                        }
                    }

                    override fun onCancelled(databaseError: DatabaseError) {
                    }
                })

            }
        } else {

            binding.btnJoinRide.visibility = View.INVISIBLE
        }

        return binding.root
    }

    private fun loadImageIntoImageView() {
        Glide.with(binding.imDriverPic.context)
            .load(viewModel.picture.value)
            .circleCrop()
            .into(binding.imDriverPic)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}