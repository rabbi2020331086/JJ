package com.journeyjunctionxml
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.navigation.NavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class my_photos_adapter(private val userList: List<String>) :
    RecyclerView.Adapter<my_photos_adapter.searchViewHolder>() {
    inner class searchViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val image = itemView.findViewById<ImageView>(R.id.post_item_image)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): searchViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.my_photos_layout,parent,false)
        return searchViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: searchViewHolder, position: Int) {
        val url = userList[position]
        if (url != null && url != "null" && url != "") {
            GlobalScope.launch(Dispatchers.Main) {
                val bitmap = withContext(Dispatchers.IO) {
                    downloadBitmap(url)
                }
                holder.image.setImageBitmap(bitmap)
            }
        } else {
            holder.image.visibility = View.GONE
        }
    }
    override fun getItemCount(): Int {
        return userList.size
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
