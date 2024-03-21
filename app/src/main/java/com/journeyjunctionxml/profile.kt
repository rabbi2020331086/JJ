package com.journeyjunctionxml
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
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
    var gender_selected = false
    lateinit var email: TextView
    lateinit var phone: TextView
    lateinit var address: TextView
    lateinit var nationality: TextView
    lateinit var interests: TextView
    lateinit var gender: TextView
    lateinit var destinations: TextView
    lateinit var seasons: TextView
    lateinit var duration: TextView
    val uid = Firebase.getCurrentUser()?.uid.toString()
    lateinit var budget: TextView
    lateinit var editProfile: Button
    private var x: Uri? = null
    val GALLERY_REQUEST_CODE = 1
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile, container, false)
        editProfile = view.findViewById(R.id.EditProfile)
        email = view.findViewById(R.id.email)
        phone = view.findViewById(R.id.phone)
        address = view.findViewById(R.id.address)
        nationality = view.findViewById(R.id.nationality)
        interests = view.findViewById(R.id.interests)
        gender = view.findViewById(R.id.gender)
        destinations = view.findViewById(R.id.destinations)
        seasons = view.findViewById(R.id.seasons)
        duration = view.findViewById(R.id.duration)
        budget = view.findViewById(R.id.budget)
        editProfile.visibility = View.GONE
        if(Firebase.getCurrentUser() == null){
            findNavController().navigate(R.id.action_profile_to_sign_in)
        }
        val timeline = view.findViewById<Button>(R.id.profile_timeline)
        timeline.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_timeLine)
        }
        val photos = view.findViewById<Button>(R.id.profile_photos_button)
        photos.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_my_photos2)
        }
        profileImage = view.findViewById(R.id.imageViewProfile)
        profilename = view.findViewById(R.id.profile_name)
        profileImage.setOnClickListener {
            if(uid == DataClass.profileUID){
                val currentUser = Firebase.getCurrentUser()
                if (currentUser == null) {
                    findNavController().navigate(R.id.action_profile_to_sign_in)
                } else {
                    ProfilePicturePopUp(requireContext())
                }
            }
            else{
                return@setOnClickListener
            }
        }
        LoadProfile()


        editProfile.setOnClickListener {
            editProfile(requireContext())
        }
        return view
    }
    private fun editProfile(context: Context){
        val inflater = LayoutInflater.from(context)
        val dialogView = inflater.inflate(R.layout.edit_profile, null)
        val dialog = Dialog(context)
        dialog.setContentView(dialogView)
        dialog.show()
        val back_button = dialogView.findViewById<ImageButton>(R.id.edit_field_back_button)
        val done = dialogView.findViewById<Button>(R.id.create_post_done_button)
        val spinner = dialogView.findViewById<Spinner>(R.id.journey_page_popup_spinner)
        val text = dialogView.findViewById<EditText>(R.id.journey_page_popup_edit_text)
        val options = arrayOf("Select field","Phone","Address","Nationality", "Gender","Destinations", "Interests","Seasons","Duration","Budget")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        var selected = ""
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        back_button.setOnClickListener {
            dialog.dismiss()
        }
        done.setOnClickListener {
            if(selected == "" || selected == "Select field"){
                Toast.makeText(requireContext(),"Please select a field", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val text = text.text
            if(text.isEmpty()){
                Toast.makeText(requireContext(),"Editfield must be filled.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(selected == "Phone"){
                Firebase.setProfile(uid,"phone",text.toString(), onCompleted = {istrue ->
                    if(istrue){
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_profile_self)
                    }
                })
            }
            if(selected == "Address"){
                Firebase.setProfile(uid,"address",text.toString(), onCompleted = {istrue ->
                    if(istrue){
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_profile_self)
                    }
                })
            }
            if(selected == "Nationality"){
                Firebase.setProfile(uid,"nationality",text.toString(), onCompleted = {istrue ->
                    if(istrue){
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_profile_self)
                    }
                })

            }
            if(selected == "Gender"){
                if(gender_selected){
                    Toast.makeText(requireContext(),"Gender can only be select once", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if(!(text.toString() == "Male" || text.toString() =="Female")){
                    Toast.makeText(requireContext(),"Only (Male, Female) allowed", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                Firebase.setProfile(uid,"gender",text.toString(), onCompleted = {istrue ->
                    if(istrue){
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_profile_self)
                    }
                })
            }
            if(selected == "Destinations"){
                Firebase.setProfile(uid,"destinations",text.toString(), onCompleted = {istrue ->
                    if(istrue){
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_profile_self)
                    }
                })

            }
            if(selected == "Interests"){
                Firebase.setProfile(uid,"interests",text.toString(), onCompleted = {istrue ->
                    if(istrue){
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_profile_self)
                    }
                })

            }
            if(selected == "Seasons"){
                Firebase.setProfile(uid,"seasons",text.toString(), onCompleted = {istrue ->
                    if(istrue){
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_profile_self)
                    }
                })

            }
            if(selected == "Duration"){
                Firebase.setProfile(uid,"duration",text.toString(), onCompleted = {istrue ->
                    if(istrue){
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_profile_self)
                    }
                })

            }
            if(selected == "Budget"){
                Firebase.setProfile(uid,"budget",text.toString(), onCompleted = {istrue ->
                    if(istrue){
                        dialog.dismiss()
                        findNavController().navigate(R.id.action_profile_self)
                    }
                })

            }
        }


        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = parent?.getItemAtPosition(position).toString()
                selected = selectedItem
                if(selected == "Gender"){
                    text.setHint("Male or Female")
                }
                else{
                    text.setHint("Write your changes")
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }
    private fun ProfilePicturePopUp(context: Context) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.profile_picture_popup_layout, null)
        val dialog = Dialog(context)
        dialog.setContentView(popupView)
        dialog.show()
        popupimage = popupView.findViewById(R.id.profile_pic_layout_images)

        popupimage.setOnClickListener {
            openGallery()
        }

        val closeButton = popupView.findViewById<View>(R.id.profile_pic_popup_button)
        closeButton.setOnClickListener {
            if (x != null) {
                Firebase.uploadImageToFirestore(x!!, requireContext(),"profile_pictures",0,"-1","name")
                dialog.dismiss()
                findNavController().navigate(R.id.action_profile_self)
            } else {
                Toast.makeText(context, "Please select a valid image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun LoadProfile(){
        val cur = Firebase.getCurrentUser();
        if(cur!=null) {
            Firebase.get_docs_info(
                "users",
                DataClass.profileUID
            ) { data ->
                if (data != null) {
                    Log.d(ContentValues.TAG, "Profile Image URL: $data")
                    val isempty = "Not set yet"
                    GlobalScope.launch(Dispatchers.Main) {
                        try {
                            val profile_image = data?.get("profile_pictures") as? String
                            val name = data?.get("name") as? String
                            var bitmap: Bitmap? = null

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
        val thisuid = DataClass.profileUID
        if(thisuid == uid){
            editProfile.visibility = View.VISIBLE
        }
        Firebase.getProfileInfo(thisuid) { list ->
            if (list.isNotEmpty() && list[0].isNotEmpty()) {
                email.text = list[0]
            } else {
                email.text = "Not set yet"
            }

            if (list.size > 1 && list[1].isNotEmpty()) {
                phone.text = list[1]
            } else {
                phone.text = "Not set yet"
            }

            if (list.size > 2 && list[2].isNotEmpty()) {
                address.text = list[2]
            } else {
                address.text = "Not set yet"
            }

            if (list.size > 3 && list[3].isNotEmpty()) {
                nationality.text = list[3]
            } else {
                nationality.text = "Not set yet"
            }

            if (list.size > 4 && list[4].isNotEmpty()) {
                interests.text = list[4]
            } else {
                interests.text = "Not set yet"
            }

            if (list.size > 5 && list[5].isNotEmpty()) {
                gender_selected = true
                gender.text = list[5]
            } else {
                gender.text = "Not set yet"
            }

            if (list.size > 6 && list[6].isNotEmpty()) {
                destinations.text = list[6]
            } else {
                destinations.text = "Not set yet"
            }

            if (list.size > 7 && list[7].isNotEmpty()) {
                seasons.text = list[7]
            } else {
                seasons.text = "Not set yet"
            }

            if (list.size > 8 && list[8].isNotEmpty()) {
                duration.text = list[8]
            } else {
                duration.text = "Not set yet"
            }

            if (list.size > 9 && list[9].isNotEmpty()) {
                budget.text = list[9]
            } else {
                budget.text = "Not set yet"
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
