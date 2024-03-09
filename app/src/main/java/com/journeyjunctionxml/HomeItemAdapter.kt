package com.journeyjunctionxml
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import com.journeyjunctionxml.homeItemViewHolder
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView

class HomeItemAdapter(private val items: List<Pair<String, PostModel>>) : RecyclerView.Adapter<homeItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): homeItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_template, parent, false)
        return homeItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: homeItemViewHolder, position: Int) {
        val (postId, postModel) = items[position]
        holder.profileName.text = postModel.profileName
        val firstCharacter: Char? = postModel.profileName.firstOrNull()
        holder.profilePic.setText(firstCharacter.toString())
        val contentImageUri = convertToUri(postModel.contentImage)
        if (contentImageUri != null) {
            holder.contentImage.setImageURI(contentImageUri)
        } else {
            holder.contentImage.visibility = View.GONE
        }
        if(postModel.contentCaption == "-1"){
            holder.contentCaption.isInvisible = true
        }
        else
            holder.contentCaption.text = postModel.contentCaption
        holder.react.setText(postModel.reactCount.toString())
    }
    fun convertToUri(any: Any?): Uri? {
        return if (any is Uri) {
            any // If it's already a Uri, return as it is
        } else if (any is String) {
            Uri.parse(any) // If it's a String, parse it to Uri
        } else {
            null // Return null for other types
        }
    }
    override fun getItemCount(): Int {
        return items.size
    }
    fun stringToBitmap(encodedString: String?): Bitmap? {
        if (encodedString.isNullOrEmpty()) {
            return null
        }
        val decodedBytes = Base64.decode(encodedString, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
    fun stringToUri(uriString: String?): Uri? {
        if (uriString.isNullOrEmpty()) {
            return null
        }
        return Uri.parse(uriString)
    }

}

