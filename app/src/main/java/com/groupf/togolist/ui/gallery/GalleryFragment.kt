package com.groupf.togolist.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.groupf.togolist.Model.LocationItem
import com.groupf.togolist.adapter.LocationAdapter
import com.groupf.togolist.databinding.FragmentGalleryBinding
import androidx.navigation.fragment.findNavController
import com.groupf.togolist.R

class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: GalleryViewModel
    private lateinit var locationRecyclerView: RecyclerView
    private lateinit var locationAdapter: LocationAdapter
    private val locationList = mutableListOf<LocationItem>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)



//        val textView: TextView = binding.textGallery
//        galleryViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        locationRecyclerView = binding.locationRecyclerView
        locationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        locationAdapter = LocationAdapter(locationList, ::onLocationClick, ::onVisitedChanged)
        locationRecyclerView.adapter = locationAdapter

        viewModel.locations.observe(viewLifecycleOwner, { locations ->
            locationList.clear()
            locationList.addAll(locations)
            locationAdapter.notifyDataSetChanged()
        })

        viewModel.fetchLocationsForList()

        return root
    }

    private fun onLocationClick(location: LocationItem) {
        val bundle = Bundle().apply {
            putStringArray("placeIds", arrayOf(location.id))
        }
        findNavController().navigate(R.id.action_galleryFragment_to_homeFragment, bundle)
    }

    private fun onVisitedChanged(location: LocationItem, visited: Boolean) {
        viewModel.updateVisitedStatus(location, visited)
        location.visited = visited // Ensure immediate local update
        locationAdapter.notifyDataSetChanged() // Notify adapter of changes
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}