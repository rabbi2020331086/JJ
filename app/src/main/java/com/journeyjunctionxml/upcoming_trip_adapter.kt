package com.journeyjunctionxml

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.journeyjunctionxml.TripsModel
import com.journeyjunctionxml.Firebase
import org.w3c.dom.Text

class upcoming_trip_adapter(private val context: Context, private val navController: NavController, private val userList: List<TripsModel>) :
    RecyclerView.Adapter<upcoming_trip_adapter.UpcomingTripViewHolder>() {

    inner class UpcomingTripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.trips_title)
        val picture: ImageView = itemView.findViewById(R.id.trip_item_image)
        val duration: TextView = itemView.findViewById(R.id.trips_item_duration)
        val date: TextView = itemView.findViewById(R.id.trips_item_date)
        val places: TextView = itemView.findViewById(R.id.trips_item_destinations)
        val budget: TextView = itemView.findViewById(R.id.trips_item_budget)
        val check_in: TextView = itemView.findViewById(R.id.trips_item_checkin)
        val vacancy: TextView = itemView.findViewById(R.id.trips_item_vacancy)
        val gender: TextView = itemView.findViewById(R.id.trips_item_gender)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UpcomingTripViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trips_layout, parent, false)
        return UpcomingTripViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: UpcomingTripViewHolder, position: Int) {
        val currentTrip = userList[position]
        holder.title.text = currentTrip.title
        holder.duration.text = currentTrip.duration
        holder.date.text = currentTrip.date
        holder.places.text = currentTrip.places
        holder.budget.text = currentTrip.budget.toString()
        holder.check_in.text = currentTrip.check_in
        holder.vacancy.text = currentTrip.vacancy
        holder.gender.text = currentTrip.gender    }
    override fun getItemCount(): Int {
        return userList.size
    }
}
