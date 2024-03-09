package com.journeyjunctionxml
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.graphics.Bitmap
import com.journeyjunctionxml.Firebase
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class get_started : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.get_started, container, false)
        val get_started_button_click = view.findViewById<Button>(R.id.get_started_button)
        val currentUser = Firebase.getCurrentUser()
        if (currentUser != null) {
            Firebase.get_docs_info(
                "users",
                currentUser.uid
            ) { data ->
                if (data != null) {
                    Firebase.name = (data?.get("name") as? String).toString()
                    Log.d(TAG,"Name: " + Firebase.name)
                }
                else{
                    Log.d(TAG,"data not found for user")
                }
            }
            Firebase.get_docs_info("users", currentUser.uid) { userInfo ->
                Firebase.idtype = userInfo?.get("type")?.toString() ?: "explorer"
            }
            if (view is ViewGroup) {
                Firebase.getFriends(requireContext(),
                    onCompleted = { isSuccess ->
                        if (isSuccess) {
                            var x = 0
                            val sz = Firebase.friends.size
                            Firebase.friends.forEach { friend ->
                                val friendUid = friend["uid"].toString()
                                Firebase.getPost(requireContext(),friendUid, 1, onCompleted = {
                                    if(isSuccess){
                                        x++
                                        if(x == sz){
//                                            Firebase.sort()
                                            Firebase.friends.clear()
                                            //gaja
                                            findNavController().navigate(R.id.action_get_started_to_home2)
                                            //gaja
                                        }
                                        Log.d(TAG,"Post success")
                                    }
                                    else{

                                    }
                                })
                            }
                        } else {
                            Toast.makeText(context, "Failed to fetch friends", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
            } else {
                Toast.makeText(requireContext(), "View is not a ViewGroup", Toast.LENGTH_SHORT).show()
            }
        }

        get_started_button_click.setOnClickListener{
            findNavController().navigate(R.id.action_get_started_to_login_preview);
        }
        return view
    }

}