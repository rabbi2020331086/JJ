package com.journeyjunctionxml

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class journey_page_members : Fragment() {
    lateinit var recyclerView: RecyclerView
    lateinit var adapter: journey_page_members_adapter
    lateinit var recyclerViewInvite: RecyclerView
    lateinit var adapterInvite: invitememberadapter
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val journeyID = DataClass.journeyUID
        val view = inflater.inflate(R.layout.journey_page_members, container, false)
        val title = view.findViewById<TextView>(R.id.journey_page_title)
        val pending_requests = view.findViewById<Button>(R.id.requested_user)
        val navController = findNavController()
        Firebase.getJourneyPageMembers(DataClass.journeyUID,"members", onComplete = {
            list ->
            if(list.isEmpty()){
                Toast.makeText(requireContext(), "No result Found!", Toast.LENGTH_SHORT).show()
            }
            else{
                recyclerView = view.findViewById(R.id.recyclerView)
                adapter = journey_page_members_adapter("members",requireContext(), navController,list)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        })
        Firebase.checkIfMemberExist("pending", journeyID, onCompleted = {istrue ->
            if(istrue)
                pending_requests.visibility = View.VISIBLE
        })
        pending_requests.setOnClickListener {
            findNavController().navigate(R.id.action_journey_page_members2_to_journey_page_pending_request)
        }
        val invite = view.findViewById<Button>(R.id.journey_page_member_add)
        invite.setOnClickListener {
            val inflater = LayoutInflater.from(context)
            val popupView = inflater.inflate(R.layout.journey_page_member_add_popup, null)
            val dialog = Dialog(requireContext())
            val uid = Firebase.getCurrentUser()?.uid.toString()
            dialog.setContentView(popupView)
            var count = 0
            Firebase.inviteFriends(uid, onCompleted = {lists ->
                val userList = mutableListOf<search_users_model>()
                lists.forEach { user ->
                    Firebase.isExistinJourney(user.uid,DataClass.journeyUID, onCompleted = {isTrue ->
                        if(!isTrue){
                            userList.add(user)
                        }
                        count++
                        if(count == lists.size){
                            recyclerViewInvite = popupView.findViewById(R.id.recyclerView)
                            adapterInvite = invitememberadapter(requireContext(),userList)
                            recyclerViewInvite.adapter = adapterInvite
                            recyclerViewInvite.layoutManager = LinearLayoutManager(requireContext())
                        }
                    })
                }
            })
            dialog.show()
        }
        return view
    }

//    private fun invitememberPopup(context: Context) {
//        val inflater = LayoutInflater.from(context)
//        val popupView = inflater.inflate(R.layout.journey_page_member_add_popup, null)
//        val dialog = Dialog(context)
//        val uid = Firebase.getCurrentUser()?.uid.toString()
//        dialog.setContentView(popupView)
//        Firebase.inviteFriends(uid, onCompleted = {lists ->
//            val userList = mutableListOf<search_users_model>()
//            lists.forEach { user ->
//                Firebase.isExistinJourney(user.uid,DataClass.journeyUID, onCompleted = {isTrue ->
//                    if(!isTrue){
//                        userList.add(user)
//                    }
//                })
//            }
//        })
//        dialog.show()
//    }
}