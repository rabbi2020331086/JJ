package com.journeyjunctionxml

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class journey_page_members : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: journey_page_members_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.journey_page_members, container, false)
        val title = view.findViewById<TextView>(R.id.journey_page_title)
        val navController = findNavController()
        Firebase.getJourneyPageMembers(DataClass.journeyUID, onComplete = {
            list ->
            if(list.isEmpty()){
                Toast.makeText(requireContext(), "No result Found!", Toast.LENGTH_SHORT).show()
            }
            else{
                recyclerView = view.findViewById(R.id.recyclerView)
                adapter = journey_page_members_adapter(requireContext(), navController,list)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        })

        return view
    }
}