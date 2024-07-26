package com.groupf.togolist.ui.slideshow

import PlaceListAdapter
import SlideshowViewModel
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.groupf.togolist.Model.PlaceListItem
import com.groupf.togolist.R
import com.groupf.togolist.databinding.FragmentSlideshowBinding
import androidx.navigation.fragment.findNavController

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SlideshowViewModel
    private lateinit var placeListAdapter: PlaceListAdapter
    private val placeLists = mutableListOf<PlaceListItem>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        viewModel = ViewModelProvider(this).get(SlideshowViewModel::class.java)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        placeListAdapter = PlaceListAdapter(placeLists) { list ->
            // Print the structure of list.places
            Log.d("SlideshowFragment", "list: ${list}")
            Log.d("SlideshowFragment", "list.places: ${list.places}")
            val placeIds = list.places.keys.toTypedArray()
            Log.d("SlideshowFragment", "placeIds: ${placeIds.joinToString(", ")}")
            val bundle = Bundle().apply {
                putStringArray("placeIds", placeIds)
            }
            findNavController().navigate(R.id.action_slideshowFragment_to_homeFragment, bundle)
        }
        binding.recyclerView.adapter = placeListAdapter

        viewModel.lists.observe(viewLifecycleOwner) { lists ->
            placeLists.clear()
            placeLists.addAll(lists)
            placeListAdapter.notifyDataSetChanged()
        }

        viewModel.fetchPlaceLists()

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
