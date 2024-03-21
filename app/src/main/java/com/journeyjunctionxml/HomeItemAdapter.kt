package com.journeyjunctionxml
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Date

class HomeItemAdapter(private val from: String,private val context: Context, private val navController: NavController, private val postList: List<PostModel>) :
    RecyclerView.Adapter<HomeItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: TextView = itemView.findViewById(R.id.post_item_profile_icon)
        val name: TextView = itemView.findViewById(R.id.post_item_profile_name)
        val caption: TextView = itemView.findViewById(R.id.post_item_content_caption)
        val image: ImageView = itemView.findViewById(R.id.post_item_image)
        val react: FrameLayout = itemView.findViewById(R.id.post_item_react_button)
        val react_done_layout: FrameLayout = itemView.findViewById(R.id.post_item_react_done_layout_button)
        val timestamp: TextView = itemView.findViewById(R.id.post_item_timestamp)
        val textView: TextView = itemView.findViewById(R.id.cnt_react)
        val delete: TextView = itemView.findViewById(R.id.delete_post)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_template, parent, false)
        return ViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.delete.visibility = View.GONE
        val currentPost = postList[position]
        holder.name.text = currentPost.profileName
        val myid = Firebase.getCurrentUser()?.uid
        val profileID = DataClass.profileUID
        val createTime = currentPost.time
        val currentTime = Calendar.getInstance().time
        val elapsedTime = calculateElapsedTime(createTime,currentTime)
        holder.timestamp.setText(elapsedTime)
        holder.icon.setOnClickListener {
            if(from == "home"){
                DataClass.profileUID = currentPost.uid
                navController.navigate(R.id.action_home2_to_profile)
            }
        }
        holder.name.setOnClickListener {
            if(from == "home"){
                DataClass.profileUID = currentPost.uid
                navController.navigate(R.id.action_home2_to_profile)
            }
        }
        if(from == "profile" && myid == profileID){
            holder.delete.visibility = View.VISIBLE
        }
        var touched = 0
        holder.delete.setOnClickListener {
            touched++
            if(touched == 1){
                Toast.makeText(context,"Press delete button again to delete the post", Toast.LENGTH_SHORT).show()
            }
            if(touched == 2){
                Firebase.deletePost(currentPost.uid,currentPost.pid, onCompleted = {istrue ->
                    Toast.makeText(context,"Post deleted successsfully", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.action_timeLine_self)
                })
            }
        }
        var isreacted = false
        holder.textView.visibility = View.GONE
        Firebase.isReacted(currentPost.uid,myid.toString(),currentPost.pid, onCompleted = {reacted ->
            if(reacted){
                isreacted = true
                holder.react.visibility = View.GONE
                holder.react_done_layout.visibility = View.VISIBLE
            }
            else{
                isreacted = false
                holder.react.visibility = View.VISIBLE
                holder.react_done_layout.visibility = View.GONE
            }
        })
        holder.icon.text = currentPost.profileName.firstOrNull().toString()
        holder.react.setOnClickListener {
            if(isreacted)
                return@setOnClickListener
            Firebase.react(myid.toString(),currentPost.uid,currentPost.pid, onCompleted = {docs ->
                Firebase.createUserReactNotification(currentPost.uid,currentPost.pid,"$docs users have reacted to your post.")
                isreacted = true
                holder.react.visibility = View.GONE
                holder.react_done_layout.visibility = View.VISIBLE
            })
        }
        if (currentPost.contentCaption != null && currentPost.contentCaption != "null" && currentPost.contentCaption != "") {
            holder.caption.setText(currentPost.contentCaption)
        }
        else{
            holder.caption.visibility = View.GONE
        }

        if (currentPost.contentImage != null && currentPost.contentImage != "null" && currentPost.contentImage != "") {
            GlobalScope.launch(Dispatchers.Main) {
                val bitmap = withContext(Dispatchers.IO) {
                    downloadBitmap(currentPost.contentImage)
                }
                holder.image.setImageBitmap(bitmap)
            }
        } else {
            holder.image.visibility = View.GONE
        }

    }
    fun calculateElapsedTime(createTimeStamp: Date, currentTime: Date): String {
        val difference = currentTime.time - createTimeStamp.time
        val minutes = difference / (1000 * 60)
        val hours = difference / (1000 * 60 * 60)
        val days = difference / (1000 * 60 * 60 * 24)
        return when {
            days > 0 -> "${days}d"
            hours > 0 -> "${hours}h"
            minutes > 0 -> "${minutes}m"
            else -> "5s"
        }
    }

    override fun getItemCount(): Int {
        return postList.size
    }
    suspend fun downloadBitmap(imageUrl: String): Bitmap {
        return withContext(Dispatchers.IO) {
            val urlConnection = URL(imageUrl).openConnection() as HttpURLConnection
            try {
                urlConnection.connect()
                val inputStream: InputStream = urlConnection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } finally {
                urlConnection.disconnect()
            }
        }
    }
}
