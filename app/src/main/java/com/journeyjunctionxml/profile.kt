package com.journeyjunctionxml

import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class profile : Fragment() {
    private lateinit var profileImage: ImageView
    private lateinit var popupimage: ImageView
    private lateinit var profilename: TextView
    private var x: Uri? = null

    val GALLERY_REQUEST_CODE = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        val view = inflater.inflate(R.layout.profile, container, false)
        profileImage = view.findViewById<ImageView>(R.id.imageViewProfile)
        profilename = view.findViewById(R.id.profile_name)
        val logout = view.findViewById<Button>(R.id.logout)
        logout.setOnClickListener {
            Firebase.logout()
            findNavController().navigate(R.id.action_profile_to_sign_in)
        }

        profileImage.setOnClickListener {
            val currentUser = Firebase.getCurrentUser()
            if (currentUser == null) {
                findNavController().navigate(R.id.action_profile_to_sign_in)
            } else {
                ProfilePicturePopUp()
            }
        }
        LoadProfile()
        return view
    }

    private fun ProfilePicturePopUp() {
        val inflater = LayoutInflater.from(context)
        val parent: ViewGroup? = null
        val popupView = inflater.inflate(R.layout.profile_picture_popup_layout, parent, false)
        popupimage = popupView.findViewById<ImageView>(R.id.profile_pic_layout_images)

        popupimage.setOnClickListener {
            openGallery()
        }

        val popupWindow = PopupWindow(
            popupView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        popupWindow.isFocusable = true
        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.showAsDropDown(profileImage)

        val closeButton = popupView.findViewById<View>(R.id.profile_pic_popup_button)
        closeButton.setOnClickListener {
            if(x != null) {
                Firebase.uploadImageToFirestore(x!!,requireContext())
                popupWindow.dismiss()
                LoadProfile()
            }
            else{
                Toast.makeText(context,"Please select a valid images",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
        }
    }
    private fun LoadProfile(){
        val cur = Firebase.getCurrentUser();
        if(cur!=null) {
            Firebase.get_docs_info(
                "users",
                cur.uid
            ) { data ->
                if (data != null) {
                    Log.d(ContentValues.TAG, "Profile Image URL: $data")
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            val profile_image = data?.get("profile_picture") as? String
                            val name = data?.get("name") as? String
                            var bitmap: Bitmap? = null  // Declare bitmap here and initialize it to null

                            if (profile_image != null) {
                                bitmap = withContext(Dispatchers.IO) {
                                    downloadBitmap(profile_image.toString())
                                }
                                profileImage.setImageBitmap(bitmap)
                            }
                            else{
                                val drawableResourceId = R.drawable.default_profile_picture
                                profileImage.setImageResource(drawableResourceId)
                            }
                            profilename.setText(name.toString())
                        } catch (e: Exception) {
                            Log.e("LoadImage", "Error loading image from URL: $data", e)
                        }
                    }
                }
                else{
                    Log.d(TAG,"data not found for user")
                }
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
    suspend fun downloadBitmap(imageUrl: String): Bitmap {
        val urlConnection = URL(imageUrl).openConnection() as HttpURLConnection
        try {
            urlConnection.connect()
            val inputStream: InputStream = urlConnection.inputStream
            return BitmapFactory.decodeStream(inputStream)
        } finally {
            urlConnection.disconnect()
        }
    }

}
