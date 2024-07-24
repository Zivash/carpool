package com.example.carpoolapp.ui.my_rides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carpoolapp.R
import com.example.carpoolapp.databinding.MyRidesModeBinding

class MyRidesModeFragment : Fragment() {

    private var _binding: MyRidesModeBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = MyRidesModeBinding.inflate(inflater, container, false)

        binding.bottomNavigationView.menu.findItem(R.id.rides).apply {
            isChecked = true
        }

        binding.bottomNavigationView.setOnItemSelectedListener {

            val userBundle = bundleOf("user_id" to arguments?.getString("user_id"))

            when (it.itemId) {
                R.id.find -> findNavController().navigate(
                    R.id.action_myRidesModeFragment_to_findFragment,
                    userBundle
                )

                R.id.add -> findNavController().navigate(
                    R.id.action_myRidesModeFragment_to_addRideFragment,
                    userBundle
                )

                R.id.logout -> {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(getString(R.string.log_out_title))
                    builder.setMessage(getString(R.string.sure_log_out))

                    builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_myRidesModeFragment_to_loginFragment)
                    }
                    builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        binding.bottomNavigationView.menu.findItem(R.id.rides).apply {
                            isChecked = true
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

        binding.btnAsDriver.setOnClickListener {

            findNavController().navigate(
                R.id.action_myRidesModeFragment_to_rideDetailsFragment,
                bundleOf("mode" to "Driver", "user_id" to arguments?.getString("user_id"))
            )
        }

        binding.btnAsPassenger.setOnClickListener {

            findNavController().navigate(
                R.id.action_myRidesModeFragment_to_rideDetailsFragment,
                bundleOf("mode" to "Passenger", "user_id" to arguments?.getString("user_id"))
            )
        }

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}