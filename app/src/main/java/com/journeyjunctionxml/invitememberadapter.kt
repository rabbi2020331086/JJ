package com.journeyjunctionxml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.widget.Button
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.navigation.NavController
class invitememberadapter(private val context: Context, private val userList: List<search_users_model>) :
    RecyclerView.Adapter<invitememberadapter.searchViewHolder>() {
    inner class searchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: TextView = itemView.findViewById(R.id.search_item_icon)
        val inviteFriend: Button = itemView.findViewById(R.id.invite_button)
        val name: TextView = itemView.findViewById(R.id.feed_search_title)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): searchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.journey_page_add_member_popup_item,parent,false)
        return searchViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: searchViewHolder, position: Int) {
        val currentuser = userList[position]
        holder.name.setText(currentuser.name)
        holder.icon.text = currentuser.name.firstOrNull().toString()
    }
    override fun getItemCount(): Int {
        return userList.size
    }
}
