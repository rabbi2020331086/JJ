package com.journeyjunctionxml

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
class my_photos : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: my_photos_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.my_photos, container, false)
        val myID = Firebase.getCurrentUser()?.uid.toString()
        Firebase.getPhotos(myID, onComplete = {list ->
            recyclerView = view.findViewById(R.id.recyclerView)
            adapter = my_photos_adapter(list)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        })
        return view;
    }
}