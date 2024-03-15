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

class sent_req : Fragment() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: sent_req_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.sent_req, container, false)
        val myuid = Firebase.getCurrentUser()?.uid.toString()
        val navController = findNavController()
        Firebase.getFriends("friend_requests",onComplete = {list ->
            if(!list.isEmpty()){

                Log.d(ContentValues.TAG,"Edittext work fine")
                recyclerview = view.findViewById(R.id.recyclerView)
                adapter = sent_req_adapter(navController,list)
                recyclerview.adapter = adapter
                recyclerview.layoutManager = LinearLayoutManager(requireContext())
            }
        })
        return view
    }
}