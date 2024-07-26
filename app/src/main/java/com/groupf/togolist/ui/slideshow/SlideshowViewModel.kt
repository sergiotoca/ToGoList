import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.groupf.togolist.Model.PlaceListItem

class SlideshowViewModel : ViewModel() {
    private val _lists = MutableLiveData<List<PlaceListItem>>()
    val lists: LiveData<List<PlaceListItem>> get() = _lists

    fun fetchPlaceLists() {
        val database = FirebaseDatabase.getInstance()
        val currentUser = FirebaseAuth.getInstance().currentUser
        val lists = mutableListOf<PlaceListItem>()

        if (currentUser != null) {
            val userId = currentUser.uid
            val listsReference = database.getReference("UserLists").child(userId)

            listsReference.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lists.clear()
                    for (listSnapshot in snapshot.children) {
                        val placeMap = mutableMapOf<String, Boolean>()
                        for (placeSnapshot in listSnapshot.children) {
                            placeMap[placeSnapshot.key ?: ""] = placeSnapshot.value as Boolean
                        }
                        val listItem = PlaceListItem(
                            id = listSnapshot.key ?: "",
                            name = listSnapshot.key ?: "",
                            placeCount = placeMap.size,
                            places = placeMap
                        )
                        lists.add(listItem)
                    }
                    _lists.value = lists
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}
