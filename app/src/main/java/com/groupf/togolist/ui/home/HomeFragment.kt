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
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
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
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
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
    private lateinit var homeViewModel: HomeViewModel

    private var _binding: FragmentHomeBinding? = null

    private lateinit var mapFragment: SupportMapFragment

    //Location
    private lateinit var locationRequest:com.google.android.gms.location.LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        _binding!!.fab.setOnClickListener { showSearchDialog() }

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        init()

        // Obtain the ChildMapFragment and get notified when the map is ready to be used.
        mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        return root
    }

    private fun init() {
        locationRequest = com.google.android.gms.location.LocationRequest()
        locationRequest.setPriority(com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest.setFastestInterval(3000)
        locationRequest.interval = 5000
        locationRequest.setSmallestDisplacement(10f)

        locationCallback = object: LocationCallback() {

            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                val newPos = LatLng(locationResult!!.lastLocation.latitude,locationResult!!.lastLocation.longitude )
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos,18f))
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showSearchDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Search")

        // Set up the input
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        builder.setView(input)

        // Set up the buttons
        builder.setPositiveButton("Search") { _, _ ->
            val searchText = input.text.toString()
            performSearch(searchText)
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun performSearch(searchText: String) {
        // Here you can add the code to perform the search
        // For now, we will just show a toast with the search text
        Toast.makeText(requireContext(), "Searching for: $searchText", Toast.LENGTH_SHORT).show()
        var latLng = getCoordinatesFromAddress(requireContext(), searchText)
        if(latLng != null)
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))

    }

    fun getCoordinatesFromAddress(context: Context, addressString: String): LatLng? {
        val geocoder = Geocoder(context)
        try {
            val addressList = geocoder.getFromLocationName(addressString, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                return latLng
            } else {
                // Handle case where no address found
                Toast.makeText(context, "No location found", Toast.LENGTH_SHORT).show()
                return null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun saveLocation(latLng: LatLng) {
        // Logic to save the location, e.g., to a database or a local list
        Toast.makeText(requireContext(), "Location saved", Toast.LENGTH_SHORT).show()
        // Handle the saving to store into the users database
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        //Request permission
        Dexter.withContext(requireContext())
            .withPermissions(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        Log.d("Permissions", "All permissions are granted.")
                        //Enable Button
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
                                    }
                                }
                            true
                        }
                        mMap.setOnMapClickListener { latLong ->
                            var marker = mMap.addMarker(MarkerOptions().position(latLong).title("User Marker"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong))
                            // Show Snackbar with a confirmation action
                            Snackbar.make(requireView(), "Save this location?", Snackbar.LENGTH_LONG)
                                .setAction("Save") {
                                    // Handle the save action
                                    saveLocation(latLong)
                                    Toast.makeText(context, "Location saved!", Toast.LENGTH_SHORT).show()
                                }
                                .setActionTextColor(ResourcesCompat.getColor(resources, R.color.colorPrimary, null))
                                .addCallback(object : Snackbar.Callback() {
                                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                                        if (event == DISMISS_EVENT_ACTION) {
                                            // Snackbar action was pressed
                                        } else {
                                            // Snackbar dismissed without action, remove the marker
                                            marker?.remove()
                                        }
                                    }
                                })
                                .show()

                        }
                        mMap.setOnMarkerClickListener { marker ->
                            marker.remove()
                            true
                        }

                    } else {
                        Log.d("Permissions", "Not all permissions are granted.")
                    }

                    if (report.isAnyPermissionPermanentlyDenied) {
                        // Handle the case when permission is permanently denied
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
            val success = googleMap.setMapStyle((MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.maps_style)))
            if(!success)
                Log.e("EDMT_ERROR", "Style parsing error")
        }catch (e:Resources.NotFoundException)
        {

        }

         //Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }
}