package com.journeyjunctionxml

import android.content.ContentValues
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class all_journeys : Fragment() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: search_journey_adapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.all_journeys, container, false)
        val navController = findNavController()
        val edittext = view.findViewById<EditText>(R.id.search_edit_text)

        Firebase.getAllJourney  (onComplete = {list ->
            Log.d(ContentValues.TAG,"Edittext work fine")
            recyclerview = view.findViewById(R.id.recyclerView)
            adapter = search_journey_adapter("all_journeys",navController,list)
            recyclerview.adapter = adapter
            recyclerview.layoutManager = LinearLayoutManager(requireContext())
        })

        edittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = edittext.text.toString()
                if(searchText.isEmpty()) {
                    Firebase.getAllJourney(onComplete = {list ->
                        Log.d(ContentValues.TAG,"Edittext work fine")
                        recyclerview = view.findViewById(R.id.recyclerView)
                        adapter = search_journey_adapter("all_journeys",navController,list)
                        recyclerview.adapter = adapter
                        recyclerview.layoutManager = LinearLayoutManager(requireContext())
                    })
                } else {
                    Firebase.getJourneybySearch(searchText,"check_in", onCompleted = {list ->
                        Log.d(ContentValues.TAG,"Edittext work fine")
                        recyclerview = view.findViewById(R.id.recyclerView)
                        adapter = search_journey_adapter("all_journeys",navController,list)
                        recyclerview.adapter = adapter
                        recyclerview.layoutManager = LinearLayoutManager(requireContext())
                })

                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })


        return view
    }
}