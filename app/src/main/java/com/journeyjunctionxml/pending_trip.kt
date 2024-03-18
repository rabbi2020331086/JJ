package com.journeyjunctionxml

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class pending_trip : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: upcoming_trip_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.pending_trip, container, false)
        val navController = findNavController()
        val uid = Firebase.getCurrentUser()?.uid.toString()
        Firebase.get_upcoming_tour(requireContext(),"pending", uid, onComplete = {
                list ->
            if(!list.isEmpty()){
                recyclerView = view.findViewById(R.id.pending_trips_recycler_view)
                adapter = upcoming_trip_adapter("pending",requireContext(), navController,list)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        })


        return view
    }
}