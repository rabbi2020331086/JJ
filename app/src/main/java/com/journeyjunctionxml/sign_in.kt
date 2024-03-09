package com.journeyjunctionxml

import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText

class sign_in : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(Firebase.getCurrentUser() != null) {
            findNavController().navigate(R.id.action_sign_in_to_home2)
        }
        val view = inflater.inflate(R.layout.sign_in, container, false)
        val signinbutton: Button = view.findViewById(R.id.sign_in)

        signinbutton.setOnClickListener{
            if(Firebase.getCurrentUser() != null) {
                findNavController().navigate(R.id.action_sign_in_to_home2)
            }
            val emailTextInputEditText: TextInputEditText = view.findViewById(R.id.sign_in_email)
            val passwordEditText: TextInputEditText = view.findViewById(R.id.sign_in_password)
            val password = passwordEditText.text.toString()
            val email = emailTextInputEditText.text.toString()
            if(email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Fill email and password!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Firebase.signInWithEmailPassword(email, password,
                onSuccess = {
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
                    findNavController().navigate(R.id.action_sign_in_to_home2)
                },
                onFailure = { exception ->
                    Log.w(TAG, "signInWithEmail:failure", exception)
                    Toast.makeText(requireContext(), "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            )
        }

        val forgot : Button = view.findViewById(R.id.sign_in_forgot_button)
        val signup : TextView = view.findViewById(R.id.sign_in_sign_up_text)

        forgot.setOnClickListener{
            findNavController().navigate(R.id.action_sign_in_to_forgot_password)
        }

        signup.setOnClickListener{
            findNavController().navigate(R.id.action_sign_in_to_create_account)
        }

        return view
    }

}