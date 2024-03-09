package com.journeyjunctionxml

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class search_page : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: searchItemAdapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_page, container, false)
        val search = view.findViewById<ImageButton>(R.id.searching)
        val edittext = view.findViewById<EditText>(R.id.search_edit_text)
        val switch_to_journey_search = view.findViewById<Button>(R.id.search_page_journey)
        val navController = findNavController()
        edittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchText = edittext.text.toString()
                if(searchText.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a search query", Toast.LENGTH_SHORT).show()
                } else {
                    Firebase.getUsersByName(searchText, onComplete = {
                            list ->
                        if(list.isEmpty()){
                            Toast.makeText(requireContext(), "No result Found!", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            recyclerView = view.findViewById(R.id.searchPagerecyclerView)
                            adapter = searchItemAdapter(requireContext(), navController,list)
                            recyclerView.adapter = adapter
                            recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        }
                    })
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        switch_to_journey_search.setOnClickListener {
            findNavController().navigate(R.id.action_search_page_to_search_journey)
        }
        return view
    }

}