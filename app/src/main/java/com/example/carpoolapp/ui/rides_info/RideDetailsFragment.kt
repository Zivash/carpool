package com.example.carpoolapp.ui.rides_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.carpoolapp.R
import com.example.carpoolapp.data.models.Ride
import com.example.carpoolapp.databinding.RidesDetailsLayoutBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.CountDownLatch

class RideDetailsFragment : Fragment() {
    private var _binding: RidesDetailsLayoutBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: RideDetailsViewModel by activityViewModels()

    private var mode: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = RidesDetailsLayoutBinding.inflate(inflater, container, false)
        viewModel.setLists(mutableListOf(), mutableListOf())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mode = arguments?.getString("mode")

        binding.bottomNavigationView.setOnItemSelectedListener {

            val userBundle = bundleOf("user_id" to arguments?.getString("user_id"))

            when (it.itemId) {
                R.id.add -> findNavController().navigate(
                    R.id.action_rideDetailsFragment_to_addRideFragment,
                    userBundle
                )

                R.id.rides -> findNavController().navigate(
                    R.id.action_rideDetailsFragment_to_myRidesModeFragment,
                    userBundle
                )

                R.id.find -> findNavController().navigate(
                    R.id.action_rideDetailsFragment_to_findFragment,
                    userBundle
                )

                R.id.logout -> {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(getString(R.string.log_out_title))
                    builder.setMessage(getString(R.string.sure_log_out))

                    builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_rideDetailsFragment_to_loginFragment)
                    }
                    builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        if (mode == "Driver" || mode == "Passenger")
                            binding.bottomNavigationView.menu.findItem(R.id.rides).apply {
                                isChecked = true
                            } else {
                            binding.bottomNavigationView.menu.findItem(R.id.find).apply {
                                isChecked = true
                            }
                        }

                        dialog.dismiss()
                    }

                    val dialog = builder.create()
                    dialog.show()
                    dialog.setCancelable(false)
                }
            }

            true
        }

        binding.recycler.layoutManager = LinearLayoutManager(requireActivity())

        if (mode == "Find") {
            binding.bottomNavigationView.menu.findItem(R.id.find).apply {
                isChecked = true
            }

            handleFindResult()
        } else {
            binding.bottomNavigationView.menu.findItem(R.id.rides).apply {
                isChecked = true
            }

            handlePassengerOrDriverResult(mode!!)
        }

        viewModel.rides.observe(viewLifecycleOwner) {

            binding.recycler.adapter =
                RideAdapter(
                    requireContext(),
                    viewModel.rides.value!!,
                    object : RideAdapter.RideListener {
                        override fun onItemClick(index: Int) {
                            if (mode == "Driver") {
                                handleDriver(index)
                            } else {
                                handlePassengerOrFind(index)
                            }
                        }
                    })
        }
    }

    private fun handlePassengerOrFind(index: Int) {
        val ride = (binding.recycler.adapter as RideAdapter).itemAt(index)

        val database = FirebaseDatabase.getInstance()
        val usersRef = ride.driverId?.let {
            database.getReference("Users").child(it)
        }

        usersRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    viewModel.setName(
                        dataSnapshot.child("name").getValue(String::class.java)
                    )
                    viewModel.setPhone(
                        dataSnapshot.child("phone").getValue(String::class.java)
                    )
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.setPicture(
                            dataSnapshot.child("picture")
                                .getValue(String::class.java)
                        )
                    }

                    val bundle = if (mode == "Find") {
                        bundleOf(
                            "seats" to arguments?.getInt("seats"),
                            "ride_id" to viewModel.getListId(index),
                            "user_id" to arguments?.getString("user_id"),
                            "from" to "Find"
                        )
                    } else {
                        bundleOf(
                            "from" to "Passenger"
                        )
                    }

                    findNavController().navigate(
                        R.id.action_rideDetailsFragment_to_driverInfo,
                        bundle
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    private fun handleDriver(index: Int) {
        ItemTouchHelper(object : ItemTouchHelper.Callback() {

            override fun getMovementFlags(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ) = makeFlag(
                ItemTouchHelper.ACTION_STATE_SWIPE,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            )

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val ride =
                    (binding.recycler.adapter as RideAdapter).itemAt(viewHolder.adapterPosition)
                val rideId = viewModel.getListId(index)
                viewModel.deleteRide(ride, rideId)
                binding.recycler.adapter!!.notifyItemRemoved(viewHolder.adapterPosition)

                if (viewModel.rides.value?.isEmpty() == true) {
                    findNavController().navigate(
                        R.id.action_rideDetailsFragment_to_emptyRidesFragment,
                        bundleOf("mode" to mode, "user_id" to arguments?.getString("user_id"))
                    )
                }

                removeFromDB(rideId)
            }
        }).attachToRecyclerView(binding.recycler)
    }

    private fun handleFindResult() {

        val database = FirebaseDatabase.getInstance()
        val ridesRef = database.getReference("All Rides")

        ridesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {
                    val rideList = mutableListOf<Ride>()
                    val rideIdList = mutableListOf<String>()
                    var pendingCallbacks = 0

                    for (rideSnapshot in snapshot.children) {
                        val ride = rideSnapshot.getValue(Ride::class.java)
                        ride?.let {
                            if (it.availableSeats!! >= arguments?.getInt("seats")!! &&
                                it.date == arguments?.getString("date") &&
                                (it.originLat!! - arguments?.getDouble("originLat")!!) in-1.5..1.5 &&
                                (it.originLng!! - arguments?.getDouble("originLng")!!) in-1.5..1.5 &&
                                (it.destinationLat!! - arguments?.getDouble("destinationLat")!!) in-1.5..1.5&&
                                (it.destinationLng!! - arguments?.getDouble("destinationLng")!!) in-1.5..1.5&&
                                it.driverId != arguments?.getString("user_id")
                            ) {
                                pendingCallbacks++
                                val rideKey = rideSnapshot.key

                                val ref = database.getReference("All Rides")

                                ref.addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(dataSnapshot: DataSnapshot) {

                                        rideList.add(it)
                                        rideKey?.let { key -> rideIdList.add(key) }

                                        pendingCallbacks--
                                        if (pendingCallbacks == 0) {
                                            viewModel.setLists(rideList, rideIdList)
                                        }
                                    }

                                    override fun onCancelled(databaseError: DatabaseError) {
                                        pendingCallbacks--
                                        if (pendingCallbacks == 0) {
                                            viewModel.setLists(rideList, rideIdList)
                                        }
                                    }
                                })
                            }
                        }
                    }

                    if (pendingCallbacks == 0) {
                        withContext(Dispatchers.Main) {
                            findNavController().navigate(
                                R.id.action_rideDetailsFragment_to_emptyRidesFragment,
                                bundleOf(
                                    "mode" to mode,
                                    "user_id" to arguments?.getString("user_id")
                                )
                            )
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    private fun handlePassengerOrDriverResult(mode: String) {
        val database = FirebaseDatabase.getInstance()
        val userRef = arguments?.getString("user_id")
            ?.let { database.getReference("Users").child(it).child(mode) }

        userRef?.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                CoroutineScope(Dispatchers.IO).launch {

                    if (snapshot.exists()) {
                        val rideKeys = mutableListOf<String>()
                        for (child in snapshot.children) {
                            val key = child.key
                            if (key != null) {
                                rideKeys.add(key)
                            }
                        }

                        fetchRidesByKeys(rideKeys)
                    } else {
                        withContext(Dispatchers.Main) {
                            findNavController().navigate(
                                R.id.action_rideDetailsFragment_to_emptyRidesFragment,
                                bundleOf(
                                    "mode" to mode,
                                    "user_id" to arguments?.getString("user_id")
                                )
                            )
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }

    fun fetchRidesByKeys(keys: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = FirebaseDatabase.getInstance()
            val allRidesRef = database.getReference("All Rides")
            val rideList = mutableListOf<Ride>()
            val rideIdList = mutableListOf<String>()
            val totalKeys = keys.size

            val latch = CountDownLatch(totalKeys)

            for (key in keys) {
                launch {
                    try {
                        val snapshot = allRidesRef.child(key).get().await()
                        if (snapshot.exists()) {
                            val ride = snapshot.getValue(Ride::class.java)
                            if (ride != null) {
                                if (isDateAndTimeLater(ride.date!!, ride.time!!)) {
                                    if (mode == "Passenger") {
                                        val userId = arguments?.getString("user_id")!!
                                        val userPassengerRef = database.getReference("Users")
                                            .child(userId)
                                            .child("Passenger")
                                            .child(key)

                                        val passengerSnapshot = userPassengerRef.get().await()
                                        val seatsTaken = passengerSnapshot.getValue(Int::class.java)
                                        ride.availableSeats = seatsTaken
                                    }

                                    withContext(Dispatchers.IO) {
                                        rideList.add(ride)
                                        rideIdList.add(key)
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    } finally {
                        latch.countDown()
                    }
                }
            }

            latch.await()

            withContext(Dispatchers.Main) {
                viewModel.setLists(rideList, rideIdList)
                if (rideList.size == 0) {
                    findNavController().navigate(
                        R.id.action_rideDetailsFragment_to_emptyRidesFragment,
                        bundleOf(
                            "mode" to mode,
                            "user_id" to arguments?.getString("user_id")
                        )
                    )
                }
            }
        }
    }

    fun removeFromDB(rideId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = FirebaseDatabase.getInstance()
            val allUsersRef = database.getReference("All Rides")
            val userRef = database.getReference("Users")

            allUsersRef.child(rideId).removeValue()
            arguments?.getString("user_id")?.let {
                userRef.child(it).child("Driver")
                    .child(rideId).removeValue()
            }

            removeRideFromPassengers(rideId)
        }
    }

    private fun removeRideFromPassengers(rideId: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("Users")

            userRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (userSnapshot in dataSnapshot.children) {
                        val passengerRef = userSnapshot.child("Passenger").ref

                        passengerRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(passengerSnapshot: DataSnapshot) {
                                if (passengerSnapshot.hasChild(rideId)) {
                                    passengerRef.child(rideId).removeValue()
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                            }
                        })
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                }
            })
        }
    }

    private fun isDateAndTimeLater(dateString: String, timeString: String): Boolean {
        val currentDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        if (dateString == currentDate) {
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

            return timeString > currentTime
        }

        return dateString > currentDate
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}