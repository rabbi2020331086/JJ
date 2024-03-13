package com.journeyjunctionxml

import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import com.journeyjunctionxml.Firebase
import com.journeyjunctionxml.DataClass
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController

class journey_page : Fragment() {

    private lateinit var introduction: TextView
    private lateinit var roadmap: TextView
    private lateinit var places: TextView
    private lateinit var check_in: TextView
    private lateinit var highlights: TextView
    private lateinit var title: TextView
    private lateinit var event_date: TextView
    private lateinit var from: TextView
    private lateinit var introduction_layout: LinearLayout
    private lateinit var buttons_layout: LinearLayout
    private lateinit var checkin_layout: LinearLayout
    private lateinit var destination_layout: LinearLayout
    private lateinit var roadmap_layout: LinearLayout
    private lateinit var highlights_layout: LinearLayout
    var isadmin: Boolean = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.journey_page, container, false)
        val adminbutton = view.findViewById<Button>(R.id.journey_page_manage_journey)
        introduction_layout = view.findViewById(R.id.journey_page_introduction_layout)
        checkin_layout = view.findViewById(R.id.journey_page_checi_in_layout)
        destination_layout = view.findViewById(R.id.journey_page_destinations_layout)
        roadmap_layout = view.findViewById(R.id.journey_page_roadmap_layout)
        buttons_layout = view.findViewById(R.id.journey_page_buttons_layout)
        highlights_layout = view.findViewById(R.id.journey_page_highlights_layout)
        introduction = view.findViewById(R.id.journey_page_introduction)
        roadmap = view.findViewById(R.id.journey_page_roadmap)
        places = view.findViewById(R.id.journey_page_destinations)
        check_in = view.findViewById(R.id.journey_page_check_in)
        highlights = view.findViewById(R.id.journey_page_highlights)
        title = view.findViewById(R.id.journey_page_title)
        event_date = view.findViewById(R.id.journey_page_event_date)
        from = view.findViewById(R.id.journey_page_event_from)



        val photos = view.findViewById<ImageButton>(R.id.journey_page_photos)
        val members = view.findViewById<ImageView>(R.id.journey_page_members)
        photos.setOnClickListener {
            findNavController().navigate(R.id.action_journey_page_to_journey_page_photos2)
        }
        members.setOnClickListener {
            findNavController().navigate(R.id.action_journey_page_to_journey_page_members2)
        }
        if(Firebase.getCurrentUser() == null){
            Toast.makeText(requireContext(), "Cant be found", Toast.LENGTH_SHORT).show()
        }
        val uid = Firebase.getCurrentUser()?.uid.toString()
        Firebase.getJourneyFields(DataClass.journeyUID,"owner") { owner ->
            if (owner == "null" || owner == null) {
                adminbutton.visibility = View.GONE
                Toast.makeText(requireContext(), "Cant be found", Toast.LENGTH_SHORT).show()
            } else {
                if(owner == uid){
                    adminbutton.visibility = View.VISIBLE
                    isadmin = true
                }
                else{
                    adminbutton.visibility = View.GONE
                    isadmin = false
                }
            }
        }
        Firebase.CheckMyJourney(DataClass.journeyUID,uid, onCompleted = {
            d ->
                if(!d){
                    buttons_layout.visibility = View.GONE
                }
        })
        introduction_layout.setOnClickListener {
            if(isadmin == false)
                return@setOnClickListener
            else{
                edit_field(requireContext(),"Introduction","introduction");
            }
        }
        roadmap_layout.setOnClickListener {
            if(isadmin == false)
                return@setOnClickListener
            else{
                edit_field(requireContext(),"Roadmap","roadmap");
            }
        }
        highlights_layout.setOnClickListener {
            if(isadmin == false)
                return@setOnClickListener
            else{
                edit_field(requireContext(),"Highlights","highlights");
            }
        }
        checkin_layout.setOnClickListener {
            if(isadmin == false)
                return@setOnClickListener
            else{
                edit_field(requireContext(),"Check In","check_in");
            }
        }
        destination_layout.setOnClickListener {
            if(isadmin == false)
                return@setOnClickListener
            else{
                edit_field(requireContext(),"Destinations","places");
            }
        }
        LoadJourney()
        return view
    }
    fun LoadJourney(){
        Firebase.loadJourney(requireContext(), DataClass.journeyUID) { title1, introduction1, roadmap1, places1, checkIn1, highlights1, eventDate1, from1 ->
            if (title1 == null) {
                Toast.makeText(context, "Journey Does not exist!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_journey_page_to_home2)
            } else {
                title.setText(title1)
                if(introduction1 == null && !isadmin){
                    introduction_layout.visibility = View.GONE
                }
                else if(introduction1 == null){
                    introduction.setText("Add introduction to get more explorers")
                }
                else
                    introduction.setText(introduction1)


                if(roadmap1 == null && !isadmin){
                    roadmap_layout.visibility = View.GONE
                }
                else if(roadmap1 == null){
                    roadmap.setText("Add complete roadmap to get more explorers")
                }
                else
                    roadmap.setText(roadmap1)
                places.setText(places1)
                check_in.setText(places1)
                if(highlights1 == null && !isadmin){
                    highlights_layout.visibility = View.GONE
                }
                else if(highlights1 == null){
                    highlights.setText("Add better highlights to get more explorers")
                }
                else
                    highlights.setText(highlights1)
                event_date.setText(eventDate1)
                from.setText(from1)
            }
        }
    }
    fun edit_field(context: Context, editpopup: String, edit_field: String) {
        val post_id = DataClass.journeyUID
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.journey_page_edit, null)
        val dialog = Dialog(context)
        dialog.setContentView(dialogView)
        val title = dialogView.findViewById<TextView>(R.id.journey_page_popup_title)
        val text = dialogView.findViewById<EditText>(R.id.journey_page_popup_edit_text)
        val button = dialogView.findViewById<Button>(R.id.create_post_done_button)
        val uid = Firebase.getCurrentUser()?.uid
        if(uid != null) {
            Firebase.getJourneyFields(DataClass.journeyUID,edit_field, onCompleted = {
                docs ->
                    if(docs != null && docs != "" && docs != "null"){
                        text.setText(docs)
                    }
                    else{
                        text.setHint("Give a good $editpopup to attract more explorers.")
                    }
            })
        }
        title.setText(editpopup)

        button.setOnClickListener {
            val text = text.text.toString()
            if(text.length < 200 && edit_field == "introduction" || text.length < 200 && edit_field == "highlights"){
                Toast.makeText(context, "Write at least 200 character long.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else if(text.length < 50 && edit_field == "roadmap"){
                Toast.makeText(context, "Write at least 50 character long.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            else{
                Firebase.updateJourneyField(requireContext(),post_id,edit_field,text);
                dialog.dismiss()
            }
        }

        dialog.show()
    }

}