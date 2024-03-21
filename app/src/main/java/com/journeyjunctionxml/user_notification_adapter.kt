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
import androidx.cardview.widget.CardView
import androidx.core.view.isInvisible
import androidx.navigation.NavController
import com.google.firebase.firestore.FieldValue
import java.util.Calendar
import java.util.Date

class user_notification_adapter(private val context: Context, private val navController: NavController, private val userList: List<user_notification_model>) :
    RecyclerView.Adapter<user_notification_adapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView = itemView.findViewById<CardView>(R.id.user_notification_layout)
        val notification_text = itemView.findViewById<TextView>(R.id.user_notification_text)
        val time = itemView.findViewById<TextView>(R.id.user_notification_time)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.user_notification_layout,parent,false)
        return ViewHolder(itemView)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = userList[position]
        val text = current.text
        val create_time = current.timestampField
        val currentTime = Calendar.getInstance().time
        val timeText = calculateElapsedTime(create_time,currentTime)
        holder.notification_text.setText(text)
        holder.time.setText(timeText)
    }
    fun calculateElapsedTime(createTimeStamp: Date,currentTime: Date): String {
        val difference = currentTime.time - createTimeStamp.time
        val minutes = difference / (1000 * 60)
        val hours = difference / (1000 * 60 * 60)
        val days = difference / (1000 * 60 * 60 * 24)
        return when {
            days > 0 -> "${days} days ago"
            hours > 0 -> "${hours} hour ago"
            minutes > 0 -> "${minutes} minutes ago"
            else -> "Just now"
        }
    }
    override fun getItemCount(): Int {
        return userList.size
    }
}
