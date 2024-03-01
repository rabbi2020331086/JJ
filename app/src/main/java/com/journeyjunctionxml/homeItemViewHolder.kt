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
    val profilePic: ImageView = itemView.findViewById(R.id.post_item_profile_pic)
    val contentImage: ImageView = itemView.findViewById(R.id.post_item_image)
    val addFriend: ImageButton = itemView.findViewById(R.id.post_template_add_friend)
    val react: Button = itemView.findViewById(R.id.post_item_react_button)
    val comment: Button = itemView.findViewById(R.id.post_item_comment_button)
    val share: Button = itemView.findViewById(R.id.post_item_share_button)
    
}
