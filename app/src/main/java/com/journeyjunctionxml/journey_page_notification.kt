package com.journeyjunctionxml

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class journey_page_notification : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: journey_page_notification_adapter
    var isadmin = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val uid = Firebase.getCurrentUser()?.uid.toString()
        val view = inflater.inflate(R.layout.journey_page_notification, container, false)
        val new_notice = view.findViewById<Button>(R.id.new_notice)
        val journeyID = DataClass.journeyUID
        Firebase.getJourneyFields(journeyID,"owner", onCompleted = {owner_uid ->
            if (owner_uid == uid) {
                isadmin = true
                new_notice.visibility = View.VISIBLE
            }
            else{
                new_notice.visibility = View.GONE
            }
        })
        new_notice.setOnClickListener {
            edit_field(requireContext())
        }
        Firebase.getNotifications(journeyID, onComplete = {list ->
            recyclerView = view.findViewById(R.id.recyclerView)
            adapter = journey_page_notification_adapter(requireContext(),findNavController(),list.toList())
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        })

        return view
    }


    fun edit_field(context: Context) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.journey_page_edit, null)
        val dialog = Dialog(context)
        dialog.setContentView(dialogView)
        val title = dialogView.findViewById<TextView>(R.id.journey_page_popup_title)
        val text = dialogView.findViewById<EditText>(R.id.journey_page_popup_edit_text)
        val button = dialogView.findViewById<Button>(R.id.create_post_done_button)
        val back_button = dialogView.findViewById<ImageButton>(R.id.edit_field_back_button)
        text.setHint("Write notice    ")
        title.setText("New notice")
        back_button.setOnClickListener {
            dialog.dismiss()
        }
        val uid = Firebase.getCurrentUser()?.uid.toString()
        val journeyID = DataClass.journeyUID
        var onProcess = false
        button.setOnClickListener {
            if(onProcess)
                return@setOnClickListener
            val text = text.text.toString()
            if(text.length == 0){
                Toast.makeText(context,"Notice can not be empty!", Toast.LENGTH_SHORT).show()
            }
            else{
                onProcess = true
                Firebase.getuserinfo(uid,"name", onCompleted = {name ->
                    Firebase.create_notice(journeyID,text,name,uid, onCompleted = {istrue ->
                        Toast.makeText(context,"Notice created successfully", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(R.id.action_journey_page_notification_self)
                        dialog.dismiss()
                    })
                })
            }
        }

        dialog.show()
    }
}