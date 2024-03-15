package com.journeyjunctionxml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.navigation.NavController
class friends_adapter(navController: NavController,private val userList: List<search_users_model>) :
    RecyclerView.Adapter<friends_adapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon = itemView.findViewById<TextView>(R.id.search_item_icon)
        val name = itemView.findViewById<TextView>(R.id.feed_search_title)
        val imagebutton = itemView.findViewById<ImageView>(R.id.search_item_add_friend)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_result_feed,parent,false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: friends_adapter.ViewHolder, position: Int) {
        val currentUser = userList[position]
        holder.name.setText(currentUser.name)
    }
    override fun getItemCount(): Int {
        return userList.size
    }
}
