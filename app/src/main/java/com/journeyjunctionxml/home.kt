package com.journeyjunctionxml
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.widget.AppCompatImageButton
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import com.journeyjunctionxml.DataClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class home : Fragment() {
    val GALLERY_REQUEST_CODE = 1
    //Recycler View
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeItemAdapter
    private var x: Uri? = null
    lateinit var myuid: String
    private var isSidebarVisible = false
    lateinit var popupimage : ImageView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val currentUid = Firebase.getCurrentUser()?.uid ?: "null"
        val view =  inflater.inflate(R.layout.home, container, false)
        val notification_button = view.findViewById<ImageButton>(R.id.notification_button)
        val profilebuttonclick = view.findViewById<AppCompatImageButton>(R.id.profile_button)
        val menubutton = view.findViewById<ImageButton>(R.id.menu_button)
        val searh_button = view.findViewById<ImageButton>(R.id.searchButton)
        val journeys = view.findViewById<ImageButton>(R.id.journeys)
        val navController = findNavController()
        journeys.setOnClickListener {
            findNavController().navigate(R.id.action_home2_to_all_journeys)
        }
        myuid = Firebase.getCurrentUser()?.uid.toString()
        Firebase.getFriends(requireContext()) { friends ->
            val list = mutableListOf<PostModel>()
            var cnt = 0
            friends.forEach { uid ->
                Firebase.getPost(uid) { newlist ->
                    list.addAll(newlist)
                    cnt++
                    if (cnt == friends.size) {
                        recyclerView = view.findViewById(R.id.recyclerView)
                        adapter = HomeItemAdapter(requireContext(), navController, list.toList())
                        recyclerView.adapter = adapter
                        recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    }
                }
            }
        }



        menubutton.setOnClickListener {
            val popupWindow = PopupWindow(context)
            val customView = LayoutInflater.from(context).inflate(R.layout.sidebar_option, null)
            popupWindow.contentView = customView
            popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
            popupWindow.height = WindowManager.LayoutParams.MATCH_PARENT
            popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val createPostButton = customView.findViewById<Button>(R.id.sidebar_create_post)
            val friends_button = customView.findViewById<Button>(R.id.sidebar_friend_management_button)


            friends_button.setOnClickListener {
                findNavController().navigate(R.id.action_home2_to_friend_management)
                popupWindow.dismiss()
            }



            createPostButton.setOnClickListener {
                createPost(requireContext())
                popupWindow.dismiss()
            }
            val logoutButton = customView.findViewById<Button>(R.id.sidebar_logout_button)
            logoutButton.setOnClickListener {
                Firebase.logout()
                findNavController().navigate(R.id.action_home2_to_profile)
                popupWindow.dismiss()
            }
            val photos = customView.findViewById<Button>(R.id.sidebar_photos_button)
            photos.setOnClickListener {
                findNavController().navigate(R.id.action_home2_to_my_photos)
                popupWindow.dismiss()
            }
            val closebutton = customView.findViewById<ImageButton>(R.id.sidebar_close_button)
            closebutton.setOnClickListener {
                popupWindow.dismiss()
            }
            popupWindow.showAtLocation(view, Gravity.END, 0, 0)
            val profile_button = customView.findViewById<ImageView>(R.id.sidebar_profileIcon)
            profile_button.setOnClickListener {
                findNavController().navigate(R.id.action_home2_to_profile)
                popupWindow.dismiss()
            }
            val toolspot = customView.findViewById<Button>(R.id.sidebar_discover)
            val launch_journey = customView.findViewById<Button>(R.id.sidebar_create_trip)
            val my_trip = customView.findViewById<Button>(R.id.sidebar_mytrips)
            val keeps_note = customView.findViewById<Button>(R.id.sidebar_keep_notes)
            val my_tools = customView.findViewById<Button>(R.id.sidebar_mytools)
            val settings = customView.findViewById<Button>(R.id.sidebar_settings_button)
            val help_and_support = customView.findViewById<Button>(R.id.sidebar_help_button)

            my_trip.setOnClickListener {
                findNavController().navigate(R.id.action_home2_to_my_trips)
                popupWindow.dismiss()
            }

            if (Firebase.idtype == "explorer") {
                launch_journey.visibility = View.GONE
            } else {
                toolspot.visibility = View.VISIBLE
            }
            launch_journey.setOnClickListener {
                findNavController().navigate(R.id.action_home2_to_create_journey)
                popupWindow.dismiss()
            }
        }
        searh_button.setOnClickListener {
            findNavController().navigate(R.id.action_home2_to_search_page)
        }
        profilebuttonclick.setOnClickListener {
            DataClass.profileUID = currentUid
            findNavController().navigate(R.id.action_home2_to_profile)
        }
        notification_button.setOnClickListener {
            findNavController().navigate(R.id.action_home2_to_journey_page)
        }
//        recyclerView = view.findViewById(R.id.recyclerView)
//        adapter = HomeItemAdapter()
//        recyclerView.adapter = adapter
//        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        return view;
    }
    private fun createPost(context: Context) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.create_post_popup, null)
        val dialog = Dialog(context)
        dialog.setContentView(dialogView)
        val discard = dialogView.findViewById<Button>(R.id.create_post_discard_button)
        val done = dialogView.findViewById<Button>(R.id.create_post_done_button)
        popupimage = dialogView.findViewById(R.id.create_post_image)
        val privacybutton = dialogView.findViewById<Button>(R.id.create_post_privacy_button)
        privacybutton.setOnClickListener {
            if(privacybutton.text == "Private"){
                privacybutton.setText("Public")
            }
            else{
                privacybutton.setText("Private")
            }
        }
        popupimage.setOnClickListener {
            openGallery()
        }
        discard.setOnClickListener {
            x = null
            dialog.dismiss()
        }
        done.setOnClickListener {
            val editTextCaption = dialogView.findViewById<EditText>(R.id.create_post_caption_edit_text)
            val caption = editTextCaption.text.toString()
            if (caption.isEmpty() && x == null) {
                Toast.makeText(context, "Please choose a caption or a photo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (x != null) {
                var xx = -1
                if (privacybutton.text == "Private") {
                    xx = 0
                } else {
                    xx = 1
                }
                Firebase.getuserinfo(myuid,"name", onCompleted = {name ->
                    Firebase.uploadImageToFirestore(x!!, requireContext(), "photos", xx, caption,name)
                })
                dialog.dismiss()
            }
            Log.d(TAG, caption)
        }

        dialog.show()
    }
//    private fun searchPopup(context: Context) {
//        val inflater = LayoutInflater.from(context)
//        val dialogView = inflater.inflate(R.layout.search_popup, null)
//        val dialog = Dialog(context)
//        dialog.setContentView(dialogView)
////        val editText = dialogView.findViewById<EditText>(R.id.search_edit_text)
//        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//        dialog.setCancelable(true)
//        dialog.show()
//    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            selectedImageUri?.let { uri ->
                popupimage.setImageURI(uri)
                x = uri
            }
        }
    }
}


