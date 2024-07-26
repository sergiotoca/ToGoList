package com.groupf.togolist.adapter

import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.groupf.togolist.Model.LocationItem
import com.groupf.togolist.R
import java.util.Locale

class LocationAdapter(
    private val locations: List<LocationItem>,
    private val onLocationClick: (LocationItem) -> Unit,
    private val onVisitedChanged: (LocationItem, Boolean) -> Unit
) : RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTextView: TextView = itemView.findViewById(R.id.noteTextView)
        val latLngTextView: TextView = itemView.findViewById(R.id.latLngTextView)
        val addressTextView: TextView = itemView.findViewById(R.id.addressTextView)
        val checkBox: CheckBox = itemView.findViewById(R.id.locationCheckBox)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.noteTextView.text = location.note
        holder.latLngTextView.text = "Lat: ${location.latitude}, Lng: ${location.longitude}"
        holder.checkBox.isChecked = location.visited
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (holder.checkBox.isPressed) { // Ensure it's a user-triggered change
                onVisitedChanged(location, isChecked)
            }
        }
        holder.itemView.setOnClickListener { onLocationClick(location) }

        // Convert lat/long to address
        val geocoder = Geocoder(holder.itemView.context, Locale.getDefault())
        val addresses: MutableList<Address>? = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses != null && addresses.isNotEmpty()) {
            holder.addressTextView.text = addresses[0].getAddressLine(0)
        }
    }

    override fun getItemCount() = locations.size
}
