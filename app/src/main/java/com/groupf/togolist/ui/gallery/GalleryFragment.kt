package com.groupf.togolist.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
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

        // Set up the Spinner
        val spinner: Spinner = binding.filterSpinner
        viewModel.listNames.observe(viewLifecycleOwner, { listNames ->
            val spinnerAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                listNames
            )
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinnerAdapter
        })

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedList = parent.getItemAtPosition(position).toString()
                viewModel.filterItemsByList(selectedList)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

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