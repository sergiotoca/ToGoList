package com.groupf.togolist.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.groupf.togolist.Model.LocationItem
import com.groupf.togolist.adapter.LocationAdapter
import com.groupf.togolist.databinding.FragmentGalleryBinding
import androidx.navigation.fragment.findNavController
import com.groupf.togolist.R

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var locationRecyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter
    private val locationList = mutableListOf<LocationItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textGallery
//        galleryViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        locationRecyclerView = binding.locationRecyclerView
        locationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        locationAdapter = LocationAdapter(locationList) { location ->
            val bundle = Bundle().apply {
                putFloat("latitude", location.latitude.toFloat())
                putFloat("longitude", location.longitude.toFloat())
            }
            findNavController().navigate(R.id.action_galleryFragment_to_homeFragment, bundle)
        }
        locationRecyclerView.adapter = locationAdapter

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val database = FirebaseDatabase.getInstance().reference
        val userLocationsRef = database.child("UserLocations").child(userId!!)

        userLocationsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                locationList.clear()
                for (locationSnapshot in snapshot.children) {
                    val location = locationSnapshot.getValue(LocationItem::class.java)
                    if (location != null) {
                        locationList.add(location)
                    }
                }
                locationAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database error
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}