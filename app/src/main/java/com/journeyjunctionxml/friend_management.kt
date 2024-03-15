package com.journeyjunctionxml

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class friend_management : Fragment() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: friends_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.friend_management, container, false)
        val pending_friend = view.findViewById<Button>(R.id.pending_friend_request)
        val sent_friend = view.findViewById<Button>(R.id.sent_friend_request)
        val navController = findNavController()
        Firebase.getFriends("friends",onComplete = {list ->
            if(!list.isEmpty()){
                Log.d(ContentValues.TAG,"Edittext work fine")
                recyclerview = view.findViewById(R.id.recyclerView)
                adapter = friends_adapter(navController,list)
                recyclerview.adapter = adapter
                recyclerview.layoutManager = LinearLayoutManager(requireContext())
            }
        })

        pending_friend.setOnClickListener {
            findNavController().navigate(R.id.action_friend_management_to_pending_friend_request2)
        }

        sent_friend.setOnClickListener {
            findNavController().navigate(R.id.action_friend_management_to_sent_req)
        }
        return view
    }
}