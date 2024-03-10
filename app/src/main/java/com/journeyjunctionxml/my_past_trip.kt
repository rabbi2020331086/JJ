package com.journeyjunctionxml

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class my_past_trip : Fragment() {
    private lateinit var past_recyclerView: RecyclerView
    private lateinit var past_adapter: past_trip_adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.my_past_trip, container, false)
        val upcoming_trip = view.findViewById<Button>(R.id.past_trip_upcoming_button)
        val currentUser = Firebase.getCurrentUser()
        val navController = findNavController()

        upcoming_trip.setOnClickListener {
            findNavController().navigate(R.id.action_my_past_trip_to_my_trips)
        }
        if (currentUser != null) {
            Firebase.get_past_tour(requireContext(),currentUser.uid, onComplete = {
                    list ->
                if(!list.isEmpty()){
                    past_recyclerView = view.findViewById(R.id.past_trips_recycler_view)
                    past_adapter = past_trip_adapter(requireContext(), navController,list)
                    past_recyclerView.adapter = past_adapter
                    past_recyclerView.layoutManager = LinearLayoutManager(requireContext())
                }
            })
        }



        return view
    }
}