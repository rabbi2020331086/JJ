package com.journeyjunctionxml
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class HomeItemAdapter(private val context: Context, private val navController: NavController, private val postList: List<PostModel>) :
    RecyclerView.Adapter<HomeItemAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: TextView = itemView.findViewById(R.id.post_item_profile_icon)
        val name: TextView = itemView.findViewById(R.id.post_item_profile_name)
        val caption: TextView = itemView.findViewById(R.id.post_item_content_caption)
        val addFriend: ImageButton = itemView.findViewById(R.id.post_template_add_friend)
        val image: ImageView = itemView.findViewById(R.id.post_item_image)
        val react: Button = itemView.findViewById(R.id.post_item_react_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.post_template, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentPost = postList[position]
        holder.name.text = currentPost.profileName
        holder.addFriend.visibility = View.GONE
        val myid = Firebase.getCurrentUser()?.uid
        var isreacted = false
        Firebase.isReacted(currentPost.uid,myid.toString(),currentPost.pid, onCompleted = {reacted ->
            if(reacted){
                isreacted = true
                holder.react.setText("Loved")
            }
            else{
                isreacted = false
                holder.react.setText("Love")
            }
        })
        holder.icon.text = currentPost.profileName.firstOrNull().toString()
        holder.react.setOnClickListener {

            Toast.makeText(context, "React button clicked", Toast.LENGTH_SHORT).show()
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
