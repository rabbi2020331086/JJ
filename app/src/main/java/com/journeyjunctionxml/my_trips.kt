package com.journeyjunctionxml
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class my_trips : Fragment() {
    private lateinit var upcoming_recyclerView: RecyclerView
    private lateinit var upcoming_adapter: upcoming_trip_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val navController = findNavController()
        val view = inflater.inflate(R.layout.my_trips, container, false)
        val currentUser = Firebase.getCurrentUser()
        val past_tour = view.findViewById<Button>(R.id.my_trips_past_tour_button)
        val pendingTrip = view.findViewById<Button>(R.id.my_trips_pending_tour)


        past_tour.setOnClickListener {
            findNavController().navigate(R.id.action_my_trips_to_my_past_trip)
        }
        pendingTrip.setOnClickListener {
            findNavController().navigate(R.id.action_my_trips_to_pending_trip)
        }
        if (currentUser != null) {
            Firebase.get_upcoming_tour(requireContext(),"own_journey", currentUser.uid, onComplete = {
                    list ->
                if(!list.isEmpty()){
                    upcoming_recyclerView = view.findViewById(R.id.upcoming_trips_recycler_view)
                    upcoming_adapter = upcoming_trip_adapter("upcoming",requireContext(), navController,list)
                    upcoming_recyclerView.adapter = upcoming_adapter
                    upcoming_recyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            })
        }

        return view
    }
}