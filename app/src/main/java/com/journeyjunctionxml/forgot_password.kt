package com.journeyjunctionxml

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class forgot_password : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val auth = Firebase.auth
        val currentuser = auth.currentUser;
        if(currentuser != null) {
            findNavController().navigate(R.id.action_forgot_password_to_home2);
        }
        return inflater.inflate(R.layout.forgot_password, container, false)
    }

}