package com.journeyjunctionxml

import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class journey_page_photos : Fragment() {
    private lateinit var recyclerview: RecyclerView
    private lateinit var adapter: journey_page_photos_adapter
    private lateinit var popupimage: ImageView
    val GALLERY_REQUEST_CODE = 1
    private var x: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.journey_page_photos, container, false)
        val addPhotos = view.findViewById<Button>(R.id.journey_page_photos_add_photo)
        val navController = findNavController()
        addPhotos.setOnClickListener {
            ProfilePicturePopUp(requireContext())
        }
        Log.d(TAG, DataClass.journeyUID)
        val uid = DataClass.journeyUID
        Firebase.getJourneyPagePhotos(uid, onCompleted = {list ->
            if(list.isEmpty()){
                Toast.makeText(requireContext(), "No result Found!", Toast.LENGTH_SHORT).show()
            }
            else{
                Log.d(TAG,"Edittext work fine")
                recyclerview = view.findViewById(R.id.recyclerView)
                adapter = journey_page_photos_adapter(navController,requireContext(),list)
                recyclerview.adapter = adapter
                recyclerview.layoutManager = LinearLayoutManager(requireContext())
            }
        })
        return view
    }

    private fun ProfilePicturePopUp(context: Context) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.profile_picture_popup_layout, null)
        val dialog = Dialog(context)
        val owner = Firebase.getCurrentUser()?.uid.toString()
        dialog.setContentView(popupView)
        dialog.show()
        popupimage = popupView.findViewById(R.id.profile_pic_layout_images)
        popupimage.setOnClickListener {
            openGallery()
        }
        val closeButton = popupView.findViewById<View>(R.id.profile_pic_popup_button)
        closeButton.setOnClickListener {
            if (x != null) {
                Firebase.uploadImageJourneyPage(x!!, requireContext(),"journey_photos${DataClass.journeyUID}", onCompleted = {
                    url ->
                        if(url == "null" || url == null || url.isEmpty() || url == ""){
                            Toast.makeText(context,"Image upload failed",Toast.LENGTH_SHORT).show()
                        }
                    else{
                        Firebase.uploadImageURLToJourney(url,owner, onCompleted = {
                            done ->
                                if(done){
                                    Toast.makeText(context,"Image successfully uploaded",Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_journey_page_photos2_self)
                                }
                                else{
                                    Toast.makeText(context,"Image upload failed",Toast.LENGTH_SHORT).show()
                                }
                        })
                    }
                })
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Please select a valid image", Toast.LENGTH_SHORT).show()
            }
        }
    }

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