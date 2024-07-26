import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.groupf.togolist.Model.PlaceListItem
import com.groupf.togolist.R

class PlaceListAdapter(
    private val lists: List<PlaceListItem>,
    private val onListClick: (PlaceListItem) -> Unit
) : RecyclerView.Adapter<PlaceListAdapter.PlaceListViewHolder>() {

    class PlaceListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val listNameTextView: TextView = itemView.findViewById(R.id.listNameTextView)
        val placeCountTextView: TextView = itemView.findViewById(R.id.placeCountTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaceListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_place_list, parent, false)
        return PlaceListViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlaceListViewHolder, position: Int) {
        val list = lists[position]
        holder.listNameTextView.text = list.name
        holder.placeCountTextView.text = "Places: ${list.placeCount}"
        holder.itemView.setOnClickListener { onListClick(list) }
    }

    override fun getItemCount() = lists.size
}
