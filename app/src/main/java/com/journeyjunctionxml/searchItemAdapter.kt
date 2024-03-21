package com.journeyjunctionxml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.widget.Toast
import androidx.navigation.NavController
class searchItemAdapter(private val context: Context, private val navController: NavController, private val userList: List<search_users_model>) :
    RecyclerView.Adapter<searchItemAdapter.searchViewHolder>() {
    inner class searchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: TextView = itemView.findViewById(R.id.search_item_icon)
        val addfriend: ImageView = itemView.findViewById(R.id.search_item_add_friend)
        val name: TextView = itemView.findViewById(R.id.feed_search_title)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): searchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.search_result_feed,parent,false)
        return searchViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: searchViewHolder, position: Int) {
        var boolean = 0
        val currentUser = userList[position]
        holder.name.text = currentUser.name
        holder.icon.text = currentUser.name.firstOrNull().toString()
        holder.addfriend.visibility = View.GONE
        val myuid = Firebase.getCurrentUser()?.uid

        val uid = currentUser.uid
        var myname =""
        Firebase.getuserinfo(myuid.toString(),"name", onCompleted = {name ->
            myname = name
        })

        if (!(myuid != null && currentUser.uid == myuid)) {
            Firebase.checkIfFriendExists(currentUser.uid, onCompleted = {
                    isFriend ->
                if(isFriend){
                    holder.addfriend.visibility = View.GONE
                }
                else{
                    Firebase.checkIfSentFriendExists(currentUser.uid, onCompleted = {isval ->
                        if(isval){
                            holder.addfriend.visibility = View.GONE
                        }
                        else{
                            boolean = 1
                            holder.addfriend.visibility = View.VISIBLE
                            holder.addfriend.setImageResource(R.drawable.add_friend_icon)
                        }
                    })
                }
            })
        }
        holder.name.setOnClickListener {
            DataClass.profileUID = currentUser.uid
            navController.navigate(R.id.action_search_page_to_profile)
        }
        holder.addfriend.setOnClickListener {
            if(boolean == 1){
                Firebase.addFriend(currentUser.uid,context, callback = {
                    done->
                        if(done){
                            Firebase.createUserNotification(uid,"\"$myname\" send you friend request.")
                            boolean = 0
                            holder.addfriend.visibility = View.VISIBLE
                            holder.addfriend.setImageResource(R.drawable.add_friend_done_icon)
                        }
                })
            }
            else{
                Toast.makeText(context, "Already friend or request sent!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }
    override fun getItemCount(): Int {
        return userList.size
    }
}
