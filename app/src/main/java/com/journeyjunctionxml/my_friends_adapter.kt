import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.journeyjunctionxml.Firebase
import com.journeyjunctionxml.R
import com.journeyjunctionxml.search_users_model

class my_friends_adapter(private val context: Context, private val friendList: List<search_users_model>) :
    RecyclerView.Adapter<my_friends_adapter.friendViewHolder>() {
    inner class friendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: TextView = itemView.findViewById(R.id.search_item_icon)
        val addfriend: ImageView = itemView.findViewById(R.id.search_item_add_friend)
        val name: TextView = itemView.findViewById(R.id.feed_search_title)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): friendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_result_feed, parent, false)
        return friendViewHolder(view)
    }

    override fun onBindViewHolder(holder: friendViewHolder, position: Int) {
        val currentUser = friendList[position]
        holder.name.text = currentUser.name
        holder.icon.text = currentUser.name.firstOrNull().toString()
        holder.addfriend.isInvisible = false
    }
    override fun getItemCount(): Int {
        return friendList.size
    }


}
