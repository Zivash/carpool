package com.example.carpoolapp.ui.find_fragment

import android.Manifest
import android.app.DatePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.carpoolapp.R
import com.example.carpoolapp.databinding.FindRideFragmentBinding
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import java.util.Calendar
import java.util.Locale

class FindRideFragment : Fragment() {
    private var _binding: FindRideFragmentBinding? = null
    private val binding
        get() = _binding!!

    private val viewModel: FindRideViewModel by viewModels()

    private lateinit var placesClient: PlacesClient

    private lateinit var autocompleteOrigin: AutocompleteSupportFragment
    private lateinit var autocompleteDestination: AutocompleteSupportFragment

    private var originClearButton: ImageView? = null
    private var destinationClearButton: ImageView? = null
    private var currentLocation: String? = null

    private val locationRequestLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) {
                handleCurrentLocation(currentLocation!!)
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.you_must_approve_permissions), Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), "AIzaSyAk0nOkC9fh2z6LXN6cyfAqMMizOzpVxT8")
        placesClient = Places.createClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FindRideFragmentBinding.inflate(inflater, container, false)

        setupAutoCompletePlaces()
        setupDateAndTimePickers()
        setupAutoCompleteSeats()

        binding.bottomNavigationView.menu.findItem(R.id.find).apply {
            isChecked = true
        }

        binding.bottomNavigationView.setOnItemSelectedListener {

            val userBundle = bundleOf("user_id" to arguments?.getString("user_id"))
            clearAll()

            when (it.itemId) {
                R.id.add -> findNavController().navigate(
                    R.id.action_findFragment_to_addRideFragment,
                    userBundle
                )

                R.id.rides -> findNavController().navigate(
                    R.id.action_findFragment_to_myRidesModeFragment,
                    userBundle
                )

                R.id.logout -> {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(getString(R.string.log_out_title))
                    builder.setMessage(getString(R.string.sure_log_out))

                    builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_findFragment_to_loginFragment)
                    }
                    builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        binding.bottomNavigationView.menu.findItem(R.id.find).apply {
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

        binding.btnCurrentLocationOrigin.setOnClickListener {

            handleCurrentLocation("Origin")
        }

        binding.btnCurrentLocationDestination.setOnClickListener {

            handleCurrentLocation("Destination")
        }

        binding.btnSearch.setOnClickListener {
            if (viewModel.originLatLng.value != null &&
                viewModel.destinationLatLng.value != null &&
                viewModel.date.value != null &&
                viewModel.seats.value != null
            ) {

                val bundle = bundleOf(
                    "originLat" to viewModel.originLatLng.value?.latitude,
                    "originLng" to viewModel.destinationLatLng.value?.longitude,
                    "destinationLat" to viewModel.originLatLng.value?.latitude,
                    "destinationLng" to viewModel.destinationLatLng.value?.longitude,
                    "date" to viewModel.date.value,
                    "seats" to viewModel.seats.value,
                    "user_id" to arguments?.getString("user_id"),
                    "mode" to "Find"
                )

                findNavController().navigate(
                    R.id.action_findFragment_to_rideDetailsFragment,
                    bundle
                )
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_input),
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        }

        binding.btnCancel.setOnClickListener {

            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(getString(R.string.clear_title))
            builder.setMessage(getString(R.string.sure_clear))

            builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                dialog.dismiss()
                clearAll()
            }
            builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                binding.bottomNavigationView.menu.findItem(R.id.find).apply {
                    isChecked = true
                }
                dialog.dismiss()
            }

            val dialog = builder.create()
            dialog.show()
            dialog.setCancelable(false)
        }

        return binding.root
    }

    private fun handleCurrentLocation(location: String) {
        currentLocation = location
        val placeFields: List<Place.Field> =
            listOf(Place.Field.LAT_LNG, Place.Field.NAME, Place.Field.ADDRESS)
        val request: FindCurrentPlaceRequest =
            FindCurrentPlaceRequest.newInstance(placeFields)

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) ==
            PackageManager.PERMISSION_GRANTED
        ) {
            if(isLocationEnabled()) {

                val placeResponse = placesClient.findCurrentPlace(request)
                placeResponse.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val response = task.result

                        if (location == "Origin") {
                            handleOriginClearButton()

                            response?.placeLikelihoods?.get(0)?.place?.latLng?.let {
                                viewModel.setOriginLatLng(it)
                            }

                            response?.placeLikelihoods?.get(0)?.place?.name?.let {
                                viewModel.setOriginName(it)
                            }

                            autocompleteOrigin =
                                childFragmentManager.findFragmentById(binding.originLocationFragment.id) as AutocompleteSupportFragment
                            autocompleteOrigin.setCountries("IL")
                            autocompleteOrigin.setText(viewModel.originName.value)
                        } else {
                            handleDestinationClearButton()

                            response?.placeLikelihoods?.get(0)?.place?.latLng?.let {
                                viewModel.setDestinationLatLng(it)
                            }

                            response?.placeLikelihoods?.get(0)?.place?.name?.let {
                                viewModel.setDestinationName(it)
                            }

                            autocompleteDestination =
                                childFragmentManager.findFragmentById(binding.destinationLocationFragment.id) as AutocompleteSupportFragment
                            autocompleteDestination.setCountries("IL")
                            autocompleteDestination.setText(viewModel.destinationName.value)
                        }
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.turn_location), Toast.LENGTH_SHORT
                ).show()
            }

        } else {
            locationRequestLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun handleOriginClearButton() {
        autocompleteOrigin.view?.post {
            if (originClearButton == null) {
                originClearButton =
                    autocompleteOrigin.view?.findViewById(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)
            }

            originClearButton?.setOnClickListener {
                viewModel.setOriginName(null)
                viewModel.setOriginLatLng(null)
                autocompleteOrigin.setText(getString(R.string.empty))
            }
        }
    }

    private fun handleDestinationClearButton() {
        autocompleteDestination.view?.post {
            if (destinationClearButton == null) {
                destinationClearButton =
                    autocompleteDestination.view?.findViewById(com.google.android.libraries.places.R.id.places_autocomplete_clear_button)
            }

            destinationClearButton?.setOnClickListener {
                viewModel.setDestinationName(null)
                viewModel.setDestinationLatLng(null)
                autocompleteDestination.setText(getString(R.string.empty))
            }
        }
    }

    private fun setupAutoCompletePlaces() {
        autocompleteOrigin =
            childFragmentManager.findFragmentById(binding.originLocationFragment.id) as AutocompleteSupportFragment
        autocompleteOrigin.setCountries("IL")

        autocompleteDestination =
            childFragmentManager.findFragmentById(binding.destinationLocationFragment.id) as AutocompleteSupportFragment
        autocompleteDestination.setCountries("IL")

        autocompleteOrigin.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
            )
        )
        autocompleteDestination.setPlaceFields(
            listOf(
                Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS_COMPONENTS
            )
        )

        autocompleteOrigin.setTypesFilter(listOf(PlaceTypes.ADDRESS))
        autocompleteDestination.setTypesFilter(listOf(PlaceTypes.ADDRESS))

        autocompleteOrigin.setHint(getString(R.string.from))
        autocompleteDestination.setHint(getString(R.string.to))

        autocompleteOrigin.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                handleOriginClearButton()

                place.latLng?.let { viewModel.setOriginLatLng(it) }
                place.name?.let { viewModel.setOriginName(it) }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
            }
        })

        autocompleteDestination.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                handleDestinationClearButton()

                place.latLng?.let { viewModel.setDestinationLatLng(it) }
                place.name?.let { viewModel.setDestinationName(it) }
            }

            override fun onError(status: com.google.android.gms.common.api.Status) {
            }
        })
    }

    private fun setupDateAndTimePickers() {
        viewModel.date.observe(viewLifecycleOwner) {
            binding.btnDate.text = viewModel.date.value
        }

        binding.btnDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                val monthDate = month + 1
                val datePicker =
                    String.format(Locale.getDefault(), "%02d/%02d/%d", day, monthDate, year)
                binding.btnDate.text = datePicker
                viewModel.setDate(datePicker)
            }

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                R.style.DialogDatePickerTheme,
                listener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            datePickerDialog.setCancelable(false)
            datePickerDialog.datePicker.minDate = calendar.timeInMillis
            datePickerDialog.show()
        }
    }

    private fun setupAutoCompleteSeats() {
        val seatsOptions = arrayOf("1", "2", "3", "4", "5")
        val arrayAdapterSeats = ArrayAdapter(requireContext(), R.layout.dropdown_item, seatsOptions)
        val autoCompleteSeats = binding.dropDownSeats
        autoCompleteSeats.setAdapter(arrayAdapterSeats)
        autoCompleteSeats.setText(getString(R.string.empty))
        autoCompleteSeats.setThreshold(5)

        autoCompleteSeats.onItemClickListener =
            AdapterView.OnItemClickListener { adapterView, _, i, _ ->
                viewModel.setSeats((adapterView.getItemAtPosition(i) as String).toInt())
            }

        viewModel.seats.observe(viewLifecycleOwner) {
            viewModel.seats.value?.let { autoCompleteSeats.setText(it.toString()) }
        }
    }

    private fun clearAll() {
        viewModel.setOriginName(null)
        viewModel.setOriginLatLng(null)
        viewModel.setDestinationName(null)
        viewModel.setDestinationLatLng(null)
        viewModel.setDate(null)
        viewModel.setSeats(null)

        autocompleteOrigin.setText(getString(R.string.empty))
        autocompleteDestination.setText(getString(R.string.empty))

        setupAutoCompleteSeats()
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onDestroyView() {
        binding.btnCurrentLocationOrigin.setOnClickListener(null)
        super.onDestroyView()
        _binding = null
    }
}