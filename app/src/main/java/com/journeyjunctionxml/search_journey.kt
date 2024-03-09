package com.journeyjunctionxml

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class search_journey : Fragment() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: search_journey_adapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.search_journey, container, false)
        val currentUser = Firebase.getCurrentUser()
        val navController = findNavController()
        val edittext = view.findViewById<EditText>(R.id.search_edit_text)
        val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
        val from = view.findViewById<RadioButton>(R.id.radio_from)
        var type = if (from.isChecked) {
            "check_in"
        } else {
            "destination_search"
        }
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            type = if (checkedId == R.id.radio_from) {
                "check_in"
            } else {
                "destination_search"
            }
        }
        edittext.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                val searchText = edittext.text.toString()
                if(searchText.isEmpty()) {
                    Toast.makeText(requireContext(), "Please enter a search query", Toast.LENGTH_SHORT).show()
                } else {
                    Firebase.getJourneybySearch(searchText,type, onCompleted = {
                            list ->
                        if(list.isEmpty()){
                            Toast.makeText(requireContext(), "No result Found!", Toast.LENGTH_SHORT).show()
                            recyclerview = view.findViewById(R.id.search_journey_recycler_view)
                            adapter = search_journey_adapter(navController,list)
                            recyclerview.adapter = adapter
                            recyclerview.layoutManager = LinearLayoutManager(requireContext())
                        }
                        else{
                            Log.d(TAG,"Edittext work fine")
                            recyclerview = view.findViewById(R.id.search_journey_recycler_view)
                            adapter = search_journey_adapter(navController,list)
                            recyclerview.adapter = adapter
                            recyclerview.layoutManager = LinearLayoutManager(requireContext())
                        }
                    })
                }
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })
        return view
    }
}