package com.journeyjunctionxml

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class pending_friend_request : Fragment() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: pending_friend_request_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.pending_friend_request, container, false)
        val navController = findNavController()
        Firebase.getFriends("pending_requests",onComplete = {list ->
            if(!list.isEmpty()){
                recyclerview = view.findViewById(R.id.recyclerView)
                adapter = pending_friend_request_adapter(requireContext(),navController,list)
                recyclerview.adapter = adapter
                recyclerview.layoutManager = LinearLayoutManager(requireContext())
            }
        })
        return view
    }

}