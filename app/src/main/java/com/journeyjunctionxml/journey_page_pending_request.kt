package com.journeyjunctionxml

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class journey_page_pending_request : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: journey_page_members_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.journey_page_pending_request, container, false)
        val journeyID = DataClass.journeyUID
        val navController = findNavController()
        var isadmin = false
        val uid = Firebase.getCurrentUser()?.uid
        Firebase.getJourneyFields(DataClass.journeyUID,"owner") { owner ->
            if (owner == "null" || owner == null || owner == "") {
                Toast.makeText(requireContext(), "Cant be found", Toast.LENGTH_SHORT).show()
            } else {
                if(owner == uid){
                    isadmin = true
                    Firebase.getJourneyPageMembers(DataClass.journeyUID,"pending", onComplete = {list ->
                        Log.d(tag,"Pending")
                        if(list.isEmpty()){
                            Toast.makeText(requireContext(), "No result Found!", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            Log.d(tag,"Pending 100%")
                            recyclerView = view.findViewById(R.id.recyclerView)
                            adapter = journey_page_members_adapter("pending",requireContext(), navController,list)
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        }
                    })
                }
                else{
                    //goto back
                }
            }
        }
        return view
    }
}