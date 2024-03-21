package com.journeyjunctionxml

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class timeLine : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: HomeItemAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.timeline, container, false)
        val profileUID = DataClass.profileUID
        val navController = findNavController()
        Firebase.getPost(profileUID, onComplete = {list ->
            recyclerView = view.findViewById(R.id.recyclerView)
            adapter = HomeItemAdapter("profile",requireContext(), navController, list.toList())
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        })
        return view
    }
}