package com.groupf.togolist.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.groupf.togolist.Model.LocationItem
import com.groupf.togolist.R

class LocationAdapter(private val locations: List<LocationItem>) :
    RecyclerView.Adapter<LocationAdapter.LocationViewHolder>() {

    class LocationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val noteTextView: TextView = itemView.findViewById(R.id.noteTextView)
        val latLngTextView: TextView = itemView.findViewById(R.id.latLngTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_location, parent, false)
        return LocationViewHolder(view)
    }

    override fun onBindViewHolder(holder: LocationViewHolder, position: Int) {
        val location = locations[position]
        holder.noteTextView.text = location.note
        holder.latLngTextView.text = "Lat: ${location.latitude}, Lng: ${location.longitude}"
    }

    override fun getItemCount(): Int {
        return locations.size
    }
}
