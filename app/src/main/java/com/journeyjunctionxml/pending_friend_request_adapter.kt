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
class pending_friend_request_adapter(private val context: Context,private val navController: NavController,private val userList: List<search_users_model>) :
    RecyclerView.Adapter<pending_friend_request_adapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon = itemView.findViewById<TextView>(R.id.search_item_icon)
        val name = itemView.findViewById<TextView>(R.id.feed_search_title)
        val imagebutton = itemView.findViewById<ImageView>(R.id.search_item_add_friend)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_result_feed,parent,false)
        return ViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: pending_friend_request_adapter.ViewHolder, position: Int) {
        val currentUser = userList[position]
        val uid = currentUser.uid
        val myid = Firebase.getCurrentUser()?.uid.toString()
        holder.icon.text = currentUser.name.firstOrNull().toString()
        holder.name.setText(currentUser.name)
        holder.imagebutton.setOnClickListener {
            Firebase.FriendRequest("pending_requests","friends",myid,uid, onCompleted = {done ->
                if(done){
                    Firebase.FriendRequest("friend_requests","friends",uid,myid, onCompleted = {done ->
                        Firebase.getuserinfo(myid,"name", onCompleted = {name ->
                            Firebase.createUserNotification(uid,"$name accepted your friend request.")
                        })
                        Toast.makeText(context, "Accepted", Toast.LENGTH_SHORT).show()
                    })
                }
            })
        }
    }


    override fun getItemCount(): Int {
        return userList.size
    }
}
