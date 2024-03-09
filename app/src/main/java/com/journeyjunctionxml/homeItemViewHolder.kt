package com.journeyjunctionxml

import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class homeItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val profileName: TextView = itemView.findViewById(R.id.post_item_profile_name)
    val contentCaption: TextView = itemView.findViewById(R.id.post_item_content_caption)
    val profilePic: TextView = itemView.findViewById(R.id.post_item_profile_icon)
    val contentImage: ImageView = itemView.findViewById(R.id.post_item_image)
    val react: Button = itemView.findViewById(R.id.post_item_react_button)
}
