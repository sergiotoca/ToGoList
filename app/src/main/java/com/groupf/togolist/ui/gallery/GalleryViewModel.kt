package com.groupf.togolist.ui.gallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.groupf.togolist.Model.LocationItem

class GalleryViewModel : ViewModel() {

    private val _locations = MutableLiveData<List<LocationItem>>()
    val locations: LiveData<List<LocationItem>> get() = _locations

    fun fetchLocationsForList() {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val locations = mutableListOf<LocationItem>()

        if (currentUser != null) {
            val userId = currentUser.uid
            val locationReference = database.getReference("UserLocations").child(userId)

            locationReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    locations.clear()
                    for (locationSnapshot in snapshot.children) {
                        val locationItem = locationSnapshot.getValue(LocationItem::class.java)
                        if (locationItem != null && !locationItem.visited) {
                            locationItem.id = locationSnapshot.key ?: ""
                            locations.add(locationItem)
                        }
                    }
                    _locations.value = locations
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun updateVisitedStatus(location: LocationItem, visited: Boolean) {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            val userId = currentUser.uid
            val locationReference = database.getReference("UserLocations").child(userId).child(location.id)

            locationReference.child("visited").setValue(visited)
        }
    }
}