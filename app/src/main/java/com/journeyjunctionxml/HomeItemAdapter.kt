package com.journeyjunctionxml
import com.journeyjunctionxml.PostModel
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.journeyjunctionxml.homeItemViewHolder


class HomeItemAdapter(private val items: List<PostModel>) : RecyclerView.Adapter<homeItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): homeItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.post_template, parent, false)
        return homeItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: homeItemViewHolder, position: Int) {
        val currentItem = items[position]
        holder.profileName.text = currentItem.profileName
        holder.contentCaption.text = currentItem.contentCaption
        holder.profilePic.setImageResource(currentItem.profilePic)
        holder.contentImage.setImageResource(currentItem.contentImage)
    }

    override fun getItemCount(): Int {
        return items.size
    }
}