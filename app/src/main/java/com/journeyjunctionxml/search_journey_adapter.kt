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
import android.widget.Button

import androidx.navigation.NavController
class search_journey_adapter(private val navController: NavController, private val userList: List<TripsModel>) :
    RecyclerView.Adapter<search_journey_adapter.search_journey_view_holder>() {
    inner class search_journey_view_holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title = itemView.findViewById<TextView>(R.id.trips_title)
        val image = itemView.findViewById<ImageView>(R.id.trip_item_image)
        val duration = itemView.findViewById<TextView>(R.id.trips_item_duration)
        val starting_date = itemView.findViewById<TextView>(R.id.trips_item_date)
        val destination = itemView.findViewById<TextView>(R.id.trips_item_destinations)
        val budget = itemView.findViewById<TextView>(R.id.trips_item_budget)
        val check_in = itemView.findViewById<TextView>(R.id.trips_item_checkin)
        val vacancy = itemView.findViewById<TextView>(R.id.trips_item_vacancy)
        val gender = itemView.findViewById<TextView>(R.id.trips_item_gender)
        val detail = itemView.findViewById<Button>(R.id.trips_item_see_detail)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): search_journey_view_holder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.trips_layout,parent,false)
        return search_journey_view_holder(itemView)
    }

    override fun onBindViewHolder(holder: search_journey_adapter.search_journey_view_holder, position: Int) {
        val currentUser = userList[position]
        holder.title.text = currentUser.title
        holder.duration.text = currentUser.duration
        holder.starting_date.text = currentUser.date
        holder.destination.text = currentUser.places
        holder.budget.text = currentUser.budget
        holder.check_in.text = currentUser.check_in
        holder.vacancy.text = currentUser.vacancy
        holder.gender.text = currentUser.gender
        holder.detail.setOnClickListener {
            DataClass.journeyUID = currentUser.uid
            Log.d(TAG,currentUser.uid)
            navController.navigate(R.id.action_search_journey_to_journey_page)
        }
    }
    override fun getItemCount(): Int {
        return userList.size
    }
}
