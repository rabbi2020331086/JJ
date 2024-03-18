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
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import java.util.Calendar
import java.util.Date

class journey_page_notification_adapter(private val context: Context, private val navController: NavController, private val userList: List<notification_model>) :
    RecyclerView.Adapter<journey_page_notification_adapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon = itemView.findViewById<TextView>(R.id.post_item_profile_icon)
        val name = itemView.findViewById<TextView>(R.id.post_item_profile_name)
        val text = itemView.findViewById<TextView>(R.id.post_item_content_caption)
        val timestamp = itemView.findViewById<TextView>(R.id.post_item_timestamp)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.notification_layout,parent,false)
        return ViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = userList[position]
        holder.name.text = current.name
        holder.text.text = current.text
        holder.icon.text = current.name.firstOrNull().toString()
        val createTimeStamp = current.timestampField
        val currentTime = Calendar.getInstance().time
        val time = calculateElapsedTime(createTimeStamp, currentTime)
        holder.timestamp.setText(time)
        Log.d(TAG, current.timestampField.toString())
    }
    fun calculateElapsedTime(createTimeStamp: Date,currentTime: Date): String {
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
        return userList.size
    }
}
