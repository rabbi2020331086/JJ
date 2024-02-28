package com.journeyjunctionxml

<<<<<<< HEAD
import android.app.Dialog
=======
>>>>>>> 876fb4e30b9db19351dd197b879ff8541648e894
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
<<<<<<< HEAD
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import android.content.Context

class home : Fragment() {
//Recycler View
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HomeItemAdapter
//Sideb

    private var isSidebarVisible = false
=======
import androidx.appcompat.widget.AppCompatImageButton
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class home : Fragment() {
>>>>>>> 876fb4e30b9db19351dd197b879ff8541648e894
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val auth = Firebase.auth

        val view =  inflater.inflate(R.layout.home, container, false)
        val profilebuttonclick = view.findViewById<AppCompatImageButton>(R.id.menu_button)
<<<<<<< HEAD
        val profileButton = view.findViewById<ImageButton>(R.id.profile_button)
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
=======
>>>>>>> 876fb4e30b9db19351dd197b879ff8541648e894
        profilebuttonclick.setOnClickListener {
            findNavController().navigate(R.id.action_home2_to_profile)
        }
        return view;

    }

<<<<<<< HEAD
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
                profilePic = R.drawable.get_started_pic, // Replace with actual drawable ID
                profileName = "Alex Johnson",
                contentCaption = "Fresh coffee in the morning.",
                contentImage = R.drawable.get_started_pic, // Replace with actual drawable ID
                reactCount = 345,
                commentCount = 67,
                shareCount = 89
            )
        )

        return itemList
    }

}

private fun createPost(context: Context) {
    val inflater = LayoutInflater.from(context)
    val dialogView = inflater.inflate(R.layout.create_post_popup, null)
    val dialog = Dialog(context)
    dialog.setContentView(dialogView)
    dialog.show()
}
=======
}
>>>>>>> 876fb4e30b9db19351dd197b879ff8541648e894
