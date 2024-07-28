package com.groupf.togolist.ui.gallery

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.groupf.togolist.Model.LocationItem

class GalleryViewModel : ViewModel() {

    private val _locations = MutableLiveData<List<LocationItem>>()
    val locations: LiveData<List<LocationItem>> get() = _locations

    private val _listNames = MutableLiveData<List<String>>()
    val listNames: LiveData<List<String>> get() = _listNames

    private val database = FirebaseDatabase.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser

    init {
        fetchLists()
    }

    private fun fetchLists() {
        if (currentUser != null) {
            val userId = currentUser.uid
            val listReference = database.getReference("UserLists").child(userId)

            listReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val lists = mutableListOf<String>()
                    for (listSnapshot in snapshot.children) {
                        val listName = listSnapshot.key
                        listName?.let { lists.add(it) }
                    }
                    _listNames.value = lists
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun fetchLocationsForList() {
        if (currentUser != null) {
            val userId = currentUser.uid
            val locationReference = database.getReference("UserLocations").child(userId)
            val locations = mutableListOf<LocationItem>()

            locationReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    locations.clear()
                    for (locationSnapshot in snapshot.children) {
                        try {
                            Log.d("GalleryViewModel", "DataSnapshot: ${locationSnapshot.value}")
                            val locationItem = locationSnapshot.getValue(LocationItem::class.java)
                            if (locationItem != null && !locationItem.visited) {
                                locationItem.id = locationSnapshot.key ?: ""
                                locations.add(locationItem)
                            }
                        } catch (e: DatabaseException) {
                            Log.e("GalleryViewModel", "Error converting data snapshot to LocationItem", e)
                        } catch (e: Exception) {
                            Log.e("GalleryViewModel", "Unknown error occurred", e)
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

    fun filterItemsByList(listName: String) {
        if (currentUser != null) {
            val userId = currentUser.uid
            val locationsReference = database.getReference("UserLocations").child(userId).orderByChild("list").equalTo(listName)

            locationsReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val locations = mutableListOf<LocationItem>()
                    for (locationSnapshot in snapshot.children) {
                        Log.d("GalleryViewModel", "DataSnapshot: ${locationSnapshot.value}")
                        try {
                            val locationItem = locationSnapshot.getValue(LocationItem::class.java)
                            locationItem?.id = locationSnapshot.key ?: ""
                            locationItem?.let { if(locationItem.visited === false)locations.add(it) }
                        } catch (e: DatabaseException) {
                            Log.e("GalleryViewModel", "Error converting data snapshot to LocationItem", e)
                        } catch (e: Exception) {
                            Log.e("GalleryViewModel", "Unknown error occurred", e)
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
        if (currentUser != null) {
            val userId = currentUser.uid
            val locationReference = database.getReference("UserLocations").child(userId).child(location.id)

            locationReference.child("visited").setValue(visited)
        }
    }
}
