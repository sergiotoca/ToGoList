package com.groupf.togolist.ui.home

import android.content.Context
import android.content.res.Resources
import android.location.Geocoder
import android.location.LocationRequest
import android.os.Bundle
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.groupf.togolist.Model.LocationItem
import com.groupf.togolist.R
import com.groupf.togolist.databinding.FragmentHomeBinding
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.IOException

class HomeFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private var placeIds: Array<String>? = null
    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null
    private lateinit var mapFragment: SupportMapFragment
    private var currentMarker: Marker? = null

    // Location
    private lateinit var locationRequest: com.google.android.gms.location.LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            placeIds = it.getStringArray("placeIds")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _binding!!.fab.setOnClickListener { showSearchDialog() }
        init()

        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root
    }

    private fun init() {
        locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
            fastestInterval = 3000
            interval = 5000
            smallestDisplacement = 10f
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                placeIds?.let { ids ->
                    fetchAndPinUnvisitedPlaces(ids)
                } ?: run {
                    // Start location updates if there are no place IDs to pin
                    val newPos = LatLng(locationResult!!.lastLocation.latitude, locationResult!!.lastLocation.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos, 18f))
                }

            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Search")

        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        builder.setPositiveButton("Search") { _, _ ->
            val searchText = input.text.toString()
            performSearch(searchText)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun performSearch(searchText: String) {
        Toast.makeText(requireContext(), "Searching for: $searchText", Toast.LENGTH_SHORT).show()
        val latLng = getCoordinatesFromAddress(requireContext(), searchText)
        if (latLng != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
            addMarker(latLng)
        } else {
            Toast.makeText(requireContext(), "Location not found", Toast.LENGTH_SHORT).show()
        }
    }

    fun getCoordinatesFromAddress(context: Context, addressString: String): LatLng? {
        val geocoder = Geocoder(context)
        try {
            val addressList = geocoder.getFromLocationName(addressString, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                return LatLng(address.latitude, address.longitude)
            } else {
                Toast.makeText(context, "No location found", Toast.LENGTH_SHORT).show()
                return null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private fun saveLocation(latLng: LatLng, note: String, list: String) {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val locationReference = database.getReference("UserLocations").child(userId)
            val listReference = database.getReference("UserLists").child(userId).child(list)
            val locationId = locationReference.push().key

            if (locationId != null) {
                val locationData = mapOf(
                    "latitude" to latLng.latitude,
                    "longitude" to latLng.longitude,
                    "note" to note,
                    "list" to list
                )

                locationReference.child(locationId).setValue(locationData)
                    .addOnSuccessListener {
                        listReference.child(locationId).setValue(true)
                        Toast.makeText(requireContext(), "Location saved to Firebase", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(requireContext(), "Failed to save location: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        Dexter.withContext(requireContext())
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        mMap.isMyLocationEnabled = true
                        mMap.uiSettings.isMyLocationButtonEnabled = true
                        mMap.setOnMyLocationButtonClickListener {
                            fusedLocationProviderClient.lastLocation
                                .addOnFailureListener { e ->
                                    Toast.makeText(context!!, e.message, Toast.LENGTH_SHORT).show()
                                }.addOnSuccessListener { location ->
                                    if (location != null) {
                                        val userLatLng = LatLng(location.latitude, location.longitude)
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f))
                                        placeIds = null
                                    }
                                }
                            true
                        }
                        mMap.setOnMapClickListener { latLong ->
                            val marker = mMap.addMarker(MarkerOptions().position(latLong).title("User Marker"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong))
                            showSaveLocationDialog(latLong, marker)
                        }
                        mMap.setOnMarkerClickListener { marker ->
                            marker.remove()
                            true
                        }

                        placeIds?.let { ids ->
                            fetchAndPinUnvisitedPlaces(ids)
                        } ?: run {
                            startLocationUpdates()
                        }
                    } else {
                        Log.d("Permissions", "Not all permissions are granted.")
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        Log.d("Permissions", "Permission permanently denied.")
                        Toast.makeText(context!!, "Permission was permanently denied!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onPermissionRationaleShouldBeShown(permissions: List<PermissionRequest>, token: PermissionToken) {
                    Log.d("Permissions", "Showing permission rationale.")
                    token.continuePermissionRequest()
                }
            }).check()

        try {
            val success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.maps_style))
            if (!success) Log.e("EDMT_ERROR", "Style parsing error")
        } catch (e: Resources.NotFoundException) {
            e.printStackTrace()
        }

    }

    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
    }

    private fun fetchAndPinUnvisitedPlaces(placeIds: Array<String>) {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val locationsReference = database.getReference("UserLocations").child(userId)

            val boundsBuilder = LatLngBounds.Builder()

            locationsReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    var hasUnvisitedPlaces = false
                    var singleLocationLatLng: LatLng? = null

                    for (placeId in placeIds) {
                        val locationSnapshot = snapshot.child(placeId)
                        val locationItem = locationSnapshot.getValue(LocationItem::class.java)
                        if (locationItem != null && locationItem.visited == false) {
                            hasUnvisitedPlaces = true
                            val latLng = LatLng(locationItem.latitude, locationItem.longitude)
                            mMap.addMarker(MarkerOptions().position(latLng).title(locationItem.note))
                            boundsBuilder.include(latLng)
                            if (placeIds.size == 1) {
                                singleLocationLatLng = latLng
                            }
                        }
                    }

                    if (hasUnvisitedPlaces) {
                        if (placeIds.size == 1 && singleLocationLatLng != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(singleLocationLatLng, 18f))
                        } else {
                            val bounds = boundsBuilder.build()
                            val padding = 100 // offset from edges of the map in pixels
                            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                            mMap.animateCamera(cameraUpdate)
                        }
                    } else {
                        Toast.makeText(requireContext(), "No unvisited places to display", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle database error
                }
            })
        } else {
            // Handle user not logged in
        }
    }


    private fun fetchUserLists(callback: (List<String>) -> Unit) {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userLists = mutableListOf<String>()

        if (currentUser != null) {
            val myReference = database.getReference("UserLists").child(currentUser.uid)
            myReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (listSnapshot in snapshot.children) {
                        val listName = listSnapshot.key
                        if (listName != null) {
                            userLists.add(listName)
                        }
                    }
                    userLists.add("Create new list")
                    callback(userLists)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Failed to fetch lists: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSaveLocationDialog(latLong: LatLng, marker: Marker?) {
        fetchUserLists { userLists ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Save Location")

            val dialogView = layoutInflater.inflate(R.layout.dialog_save_location, null)
            builder.setView(dialogView)

            val inputNote = dialogView.findViewById<EditText>(R.id.inputNote)
            val spinnerLists = dialogView.findViewById<Spinner>(R.id.spinnerLists)
            val inputList = dialogView.findViewById<EditText>(R.id.inputList)

            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, userLists).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            spinnerLists.adapter = adapter

            spinnerLists.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    inputList.visibility = if (userLists[position] == "Create new list") View.VISIBLE else View.GONE
                }

                override fun onNothingSelected(parent: AdapterView<*>) {}
            }

            builder.setPositiveButton("Save") { _, _ ->
                val note = inputNote.text.toString()
                val list = if (spinnerLists.selectedItem == "Create new list") inputList.text.toString() else spinnerLists.selectedItem.toString()
                saveLocation(latLong, note, list)
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                marker?.remove()
                dialog.cancel()
            }

            builder.show()
        }
    }

    private fun addMarker(latLong: LatLng, title: String = "Searched Location") {
        if (currentMarker != null) {
            currentMarker?.remove()
        }
        currentMarker = mMap.addMarker(MarkerOptions().position(latLong).title(title))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLong, 18f))
    }

    private fun addMarkerAndMoveCamera(latLng: LatLng) {
        mMap.addMarker(MarkerOptions().position(latLng).title("Selected Location"))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
    }
}
