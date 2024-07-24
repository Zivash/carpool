package com.example.carpoolapp.ui.rides_info

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.carpoolapp.R
import com.example.carpoolapp.databinding.EmptyRidesBinding

class EmptyRidesFragment : Fragment() {
    private var _binding: EmptyRidesBinding? = null
    private val binding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = EmptyRidesBinding.inflate(inflater, container, false)

        if (arguments?.getString("mode") == "Find") {

            binding.tvEmpty.text = getString(R.string.no_rides_search)
        }

        if (arguments?.getString("mode") == "Driver") {

            binding.tvEmpty.text = getString(R.string.no_rides_driver)
        } else if (arguments?.getString("mode") == "Passenger") {
            binding.tvEmpty.text = getString(R.string.no_rides_passenger)
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {

                    if (arguments?.getString("mode") == "Find") {
                        findNavController().navigate(
                            R.id.action_emptyRidesFragment_to_findFragment,
                            bundleOf("user_id" to arguments?.getString("user_id"))
                        )
                    } else {
                        findNavController().navigate(
                            R.id.action_emptyRidesFragment_to_myRidesModeFragment,
                            bundleOf("user_id" to arguments?.getString("user_id"))
                        )
                    }
                }
            })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}