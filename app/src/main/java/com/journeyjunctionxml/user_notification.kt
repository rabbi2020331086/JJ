package com.journeyjunctionxml

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class user_notification : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: user_notification_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.user_notification, container, false)
        val uid = Firebase.getCurrentUser()?.uid.toString()
        val navController = findNavController()
        Firebase.getUserNotification(uid, onComplete = {list ->
            recyclerView = view.findViewById(R.id.recyclerView)
            adapter = user_notification_adapter(requireContext(),navController,list)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        })


        return view
    }
}