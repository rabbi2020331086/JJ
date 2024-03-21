package com.journeyjunctionxml

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.journeyjunctionxml.TripsModel
import com.journeyjunctionxml.Firebase
import org.w3c.dom.Text

class upcoming_trip_adapter(private val type: String,private val context: Context, private val navController: NavController, private val userList: List<TripsModel>) :
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
        val detail_button: Button = itemView.findViewById(R.id.trips_item_see_detail)
        val confirm_button: Button = itemView.findViewById(R.id.trips_item_confirm)
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
        holder.budget.text = currentTrip.budget
        holder.check_in.text = currentTrip.check_in
        holder.vacancy.text = currentTrip.vacancy
        holder.gender.text = currentTrip.gender
        val journeyID = currentTrip.uid
        Log.d(TAG,"JourneyID: " + journeyID)
        val uid = Firebase.getCurrentUser()?.uid
        if(type == "pending"){
            holder.confirm_button.visibility = View.VISIBLE
            holder.confirm_button.setOnClickListener {
                if(journeyID == null || journeyID == ""){
                    Log.d(TAG,"asolei " + journeyID)
                    return@setOnClickListener
                }
                Firebase.getuserinfo(uid.toString(),"name", onCompleted = {name ->
                    Firebase.sent_request_to_journey("members",uid.toString(),name,journeyID, onCompleted = {istrue ->
                        if(istrue){
                            Firebase.move_journey_in_users_collection("pending","own_journey",uid.toString(),journeyID, onCompleted = {istrue ->
                                if(istrue){
                                    holder.confirm_button.setText("Confirmed")
                                    Toast.makeText(context,"Successfully joined the journey", Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(context,"Failed to join the journey", Toast.LENGTH_SHORT).show()
                                }
                            })
                        }
                        else{
                            Toast.makeText(context,"Failed to join the journey", Toast.LENGTH_SHORT).show()
                        }
                    })
                })
            }
        }
        else{
            holder.vacancy.setText("Available")
            holder.confirm_button.visibility = View.GONE
            holder.detail_button.setOnClickListener{
                DataClass.journeyUID = journeyID
                navController.navigate(R.id.action_my_trips_to_journey_page)
            }
        }
    }
    override fun getItemCount(): Int {
        return userList.size
    }
}
