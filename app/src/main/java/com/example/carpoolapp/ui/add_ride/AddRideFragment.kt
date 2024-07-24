package com.example.carpoolapp.ui.add_ride

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TimePicker
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
import com.example.carpoolapp.data.models.Ride
import com.example.carpoolapp.databinding.AddRideFragmentBinding
import com.example.carpoolapp.ui.MainActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.PlaceTypes
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.AutocompleteSupportFragment
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.maps.android.PolyUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.lang.Integer.parseInt
import java.util.Calendar
import java.util.Locale


class AddRideFragment : Fragment() {

    private var _binding: AddRideFragmentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddRideViewModel by viewModels()

    private lateinit var auth: FirebaseAuth

    private lateinit var mapFragment: SupportMapFragment
    private lateinit var placesClient: PlacesClient
    private lateinit var mMap: GoogleMap

    private lateinit var autocompleteOrigin: AutocompleteSupportFragment
    private lateinit var autocompleteDestination: AutocompleteSupportFragment
    private var originClearButton: ImageView? = null
    private var destinationClearButton: ImageView? = null
    private var currentLocation: String? = null

    private var price: Int? = null
    private var driverId: String? = null

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

        driverId = arguments?.getString("user_id")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = AddRideFragmentBinding.inflate(inflater, container, false)

        auth = (activity as MainActivity).auth

        binding.bottomNavigationView.menu.findItem(R.id.add).apply {
            isChecked = true
        }

        binding.bottomNavigationView.setOnItemSelectedListener {

            val userBundle = bundleOf("user_id" to driverId)
            clearAll()

            when (it.itemId) {
                R.id.find -> findNavController().navigate(
                    R.id.action_addRideFragment_to_findFragment,
                    userBundle
                )

                R.id.rides -> findNavController().navigate(
                    R.id.action_addRideFragment_to_myRidesModeFragment,
                    userBundle
                )

                R.id.logout -> {
                    val builder = AlertDialog.Builder(requireContext())
                    builder.setTitle(getString(R.string.log_out_title))
                    builder.setMessage(getString(R.string.sure_log_out))

                    builder.setPositiveButton(getString(R.string.yes)) { dialog, _ ->
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_addRideFragment_to_loginFragment)
                    }
                    builder.setNegativeButton(getString(R.string.no)) { dialog, _ ->
                        binding.bottomNavigationView.menu.findItem(R.id.add).apply {
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

        mapFragment =
            childFragmentManager.findFragmentById(binding.mapFragment.id) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
            mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
            drawRoute()
        }

        binding.btnShowRoute.setOnClickListener {
            drawRoute()
        }

        setupAutoCompletePlaces()
        setupDateAndTimePickers()
        setupAutoCompleteSeats()

        binding.btnCurrentLocationOrigin.setOnClickListener {

            handleCurrentLocation("Origin")
        }

        binding.btnCurrentLocationDestination.setOnClickListener {

            handleCurrentLocation("Destination")
        }

        binding.btnCreateRide.setOnClickListener {
            price = if (binding.etPrice.text.toString().isEmpty()) null
            else parseInt(binding.etPrice.text.toString())

            if (viewModel.originLatLng.value != null &&
                viewModel.destinationLatLng.value != null &&
                viewModel.date.value != null &&
                viewModel.time.value != null &&
                viewModel.seats.value != null &&
                price != null &&
                driverId != null
            ) {

                val ride = Ride(
                    viewModel.originLatLng.value?.latitude,
                    viewModel.originLatLng.value?.longitude,
                    viewModel.destinationLatLng.value?.latitude,
                    viewModel.destinationLatLng.value?.longitude,
                    viewModel.date.value,
                    viewModel.time.value,
                    viewModel.seats.value,
                    price,
                    driverId
                )

                val databaseReference: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference("Users")

                val databaseReferenceAllRides: DatabaseReference =
                    FirebaseDatabase.getInstance().getReference("All Rides")

                val newRideRef = databaseReferenceAllRides.push()
                newRideRef.setValue(ride)

                val pushKey = newRideRef.key

                val databaseReferenceUser = databaseReference.child(driverId!!)
                pushKey?.let {
                    databaseReferenceUser.child("Driver").child(it).setValue("")
                }

                Toast.makeText(
                    requireContext(),
                    getString(R.string.ride_created),
                    Toast.LENGTH_SHORT
                )
                    .show()

            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.invalid_input), Toast.LENGTH_SHORT
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
                binding.bottomNavigationView.menu.findItem(R.id.add).apply {
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
                drawRoute()
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
                drawRoute()
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

        viewModel.time.observe(viewLifecycleOwner) {
            binding.btnTime.text = viewModel.time.value
        }

        binding.btnTime.setOnClickListener {
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            val timePickerDialog = TimePickerDialog(
                requireContext(),
                R.style.TimePickerTheme,
                { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
                    val formattedTime = String.format(
                        Locale.getDefault(),
                        "%02d:%02d",
                        selectedHour,
                        selectedMinute
                    )
                    viewModel.setTime(formattedTime)
                },
                hour, minute, true
            )

            timePickerDialog.setCancelable(false)
            timePickerDialog.show()
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
        viewModel.setTime(null)
        viewModel.setSeats(null)
        price = null

        autocompleteOrigin.setText(getString(R.string.empty))
        autocompleteDestination.setText(getString(R.string.empty))
        binding.etPrice.setText(getString(R.string.empty))

        setupAutoCompleteSeats()
        drawRoute()
    }

    private fun drawRoute() {
        if (viewModel.originLatLng.value == null || viewModel.destinationLatLng.value == null) {

            CoroutineScope(Dispatchers.Main).launch {
                mMap.clear()

                val initialLocation = LatLng(31.046051, 34.851612)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 10f))
            }
        } else {

            val url =
                "https://maps.googleapis.com/maps/api/directions/json?origin=${viewModel.originLatLng.value!!.latitude},${viewModel.originLatLng.value!!.longitude}&destination=${viewModel.destinationLatLng.value!!.latitude},${viewModel.destinationLatLng.value!!.longitude}&key=AIzaSyAk0nOkC9fh2z6LXN6cyfAqMMizOzpVxT8"
            val request = Request.Builder().url(url).build()
            val client = OkHttpClient()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                override fun onResponse(call: Call, response: Response) {
                    val responseData = response.body?.string()
                    val jsonResponse = responseData?.let { JSONObject(it) }
                    val routes = jsonResponse?.getJSONArray("routes")
                    val overviewPolyline =
                        routes?.getJSONObject(0)?.getJSONObject("overview_polyline")
                    val encodedPoints = overviewPolyline?.getString("points")

                    val decodedPath = PolyUtil.decode(encodedPoints)

                    CoroutineScope(Dispatchers.Main).launch {
                        mMap.clear()
                        mMap.addPolyline(
                            PolylineOptions().addAll(decodedPath).color(Color.RED).width(5f)
                        )

                        mMap.addMarker(
                            MarkerOptions().position(viewModel.originLatLng.value!!)
                                .title(viewModel.originName.value)
                        )
                        mMap.addMarker(
                            MarkerOptions().position(viewModel.destinationLatLng.value!!)
                                .title(viewModel.destinationName.value)
                        )

                        val bounds = LatLngBounds.builder().include(viewModel.originLatLng.value!!)
                            .include(viewModel.destinationLatLng.value!!).build()
                        mMap.moveCamera(
                            CameraUpdateFactory.newLatLngBounds(bounds, 50)
                        )
                    }
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigationView.menu.findItem(R.id.add).apply {
            isChecked = true
        }
    }

    override fun onDestroyView() {
        binding.btnCurrentLocationOrigin.setOnClickListener(null)
        super.onDestroyView()
        _binding = null
    }
}