package com.journeyjunctionxml
import android.content.ContentValues.TAG
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.util.Log
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
        var ismember = false
        var isadmin = true
        val journeyUID = DataClass.journeyUID
        val uid = currentuser.uid
        val myuid = Firebase.getCurrentUser()?.uid.toString()
        Log.d(TAG,"is invited " + journeyUID)
        var isinvited = false
        holder.inviteFriend.setOnClickListener {
            Log.d(TAG,"Working")
            var type = ""
            var by_whom = ""
            if(isadmin){
                type = "pending"
                by_whom = "admin"
            }
            else {
                type = "invited"
                by_whom = "member"
            }
            if(!isinvited){
                var myname =""
                var journeyTitle = ""
                Firebase.getuserinfo(myuid,"name", onCompleted = {name ->
                    myname = name
                })
                Firebase.getJourneyFields(journeyUID,"title", onCompleted = {title ->
                    journeyTitle = title
                })
                Firebase.sentInviteToFriend(uid,type,by_whom,journeyUID, onCompleted = {
                    Log.d(TAG,"is invited 100%")
                    isinvited = true
                    holder.inviteFriend.setText("invited")
                    holder.inviteFriend.visibility = View.VISIBLE
                    Firebase.createUserNotification(uid,"\"$myname\" is invited to join journey \"$journeyTitle\".")
                    Toast.makeText(context,"Invitation sent",Toast.LENGTH_SHORT).show()
                })
            }
            else{
                Toast.makeText(context,"Already invited",Toast.LENGTH_SHORT).show()
            }
        }
        holder.name.setText(currentuser.name)
        holder.icon.text = currentuser.name.firstOrNull().toString()
    }
    override fun getItemCount(): Int {
        return userList.size
    }
}
