package com.journeyjunctionxml
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class journey_page_photos_adapter(private val navController: NavController, private val context: Context, private val list: List<journey_page_photos_model>) :
    RecyclerView.Adapter<journey_page_photos_adapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profile_icon: TextView = itemView.findViewById(R.id.journey_page_photos_profile_icon)
        val profile_name: TextView = itemView.findViewById(R.id.journey_page_photos_profile_name)
        val addfriend: ImageButton = itemView.findViewById(R.id.journey_page_photos_add_friend)
        val image: ImageView = itemView.findViewById(R.id.journey_page_photos_image)
        val likebutton: Button = itemView.findViewById(R.id.journey_page_photos_like)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.journey_page_photos_templete, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = list[position]
        val url = currentItem.url
        val owner = currentItem.owner
        val myuid = Firebase.getCurrentUser()?.uid
        Firebase.getuserinfo(owner,"name", onCompleted = {
            name ->
                holder.profile_name.setText(name)
                holder.profile_icon.text = name.firstOrNull().toString()
        })
        if (!(myuid != null && owner == myuid)) {
            Firebase.checkIfFriendExists(owner, onCompleted = {
                    isFriend ->
                if(isFriend){
                    holder.addfriend.isInvisible = false
                    holder.addfriend.setImageResource(R.drawable.add_friend_done_icon)
                }
                else{
                    Firebase.checkIfSentFriendExists(owner, onCompleted = {
                        if(true){
                            holder.addfriend.isInvisible = false
                            holder.addfriend.setImageResource(R.drawable.add_friend_done_icon)
                        }
                        else{
                            holder.addfriend.isInvisible = false
                            holder.addfriend.setImageResource(R.drawable.add_friend_icon)
                        }
                    })

                }
            })
        }
        var bitmap: Bitmap? = null

        if (url != null) {
            GlobalScope.launch(Dispatchers.Main) {
                val bitmap = withContext(Dispatchers.IO) {
                    downloadBitmap(url)
                }
                holder.image.setImageBitmap(bitmap)
            }
        } else {
            val drawableResourceId = R.drawable.default_profile_picture
            holder.image.setImageResource(drawableResourceId)
        }
    }
    override fun getItemCount(): Int {
        return list.size
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
