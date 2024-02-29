package com.journeyjunctionxml
import android.app.Activity
import android.app.Dialog
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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.widget.AppCompatImageButton
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class home : Fragment() {
    val GALLERY_REQUEST_CODE = 1
    //Recycler View
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeItemAdapter
    private var x: Uri? = null

    private lateinit var popupimage: ImageView
//Sideb

    private var isSidebarVisible = false
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.home, container, false)
        val profilebuttonclick = view.findViewById<AppCompatImageButton>(R.id.profile_button)
        val profileButton = view.findViewById<ImageButton>(R.id.menu_button)
        profileButton.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), profileButton)
            popupMenu.inflate(R.menu.menuoption) // Replace with the name of your menu XML file
            popupMenu.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.menu_item1 -> {
                        createPost(requireContext())
                        true
                    }
                    R.id.menu_item2 -> {
                        // Handle menu item 2 click
                        true
                    }
                    R.id.menu_item3 -> {
                        // Handle menu item 3 click
                        true
                    }
                    else -> false
                }
            }
            popupMenu.show() // Show the popup menu
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        adapter = HomeItemAdapter(getSampleItemList())
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        profilebuttonclick.setOnClickListener {
            findNavController().navigate(R.id.action_home2_to_profile)
        }
        return view;

    }

    private fun getSampleItemList(): List<PostModel> {
        val itemList = mutableListOf<PostModel>()

        itemList.add(
            PostModel(
                profilePic = R.drawable.get_started_pic, // Replace with actual drawable ID
                profileName = "John Doe",
                contentCaption = "Exploring the great outdoors!",
                contentImage = R.drawable.get_started_pic, // Replace with actual drawable ID
                reactCount = 123,
                commentCount = 45,
                shareCount = 67
            )
        )

        itemList.add(
            PostModel(
                profilePic = R.drawable.get_started_pic, // Replace with actual drawable ID
                profileName = "Jane Smith",
                contentCaption = "Nothing beats a sunset at the beach.",
                contentImage = R.drawable.get_started_pic, // Replace with actual drawable ID
                reactCount = 234,
                commentCount = 56,
                shareCount = 78
            )
        )

        itemList.add(
            PostModel(
                profilePic = R.drawable.get_started_pic,
                profileName = "Alex Johnson",
                contentCaption = "Fresh coffee in the morning.",
                contentImage = R.drawable.get_started_pic,
                reactCount = 345,
                commentCount = 67,
                shareCount = 89
            )
        )

        return itemList
    }
    private fun createPost(context: Context) {
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.create_post_popup, null)
        val dialog = Dialog(context)
        dialog.setContentView(dialogView)
        popupimage = dialogView.findViewById<ImageView>(R.id.create_post_image);
        popupimage.setOnClickListener {
            openGallery()
        }
        val discard = dialogView.findViewById<Button>(R.id.create_post_discard_button)
        val done = dialogView.findViewById<Button>(R.id.create_post_done_button)
        discard.setOnClickListener {
            x = null
            dialog.dismiss()
        }
        done.setOnClickListener {
            val editTextCaption = dialogView.findViewById<EditText>(R.id.create_post_caption_edit_text)
            val caption = editTextCaption.text.toString()
            if (x != null) {
                Firebase.uploadImageToFirestore(x!!, requireContext(),"photos", 0,caption)
                dialog.dismiss()
            }
            Log.d(TAG,caption)
        }
        dialog.show()
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


