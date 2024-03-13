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
        if(currentUser!=null){
            findNavController().navigate(R.id.action_get_started_to_home2)
        }

        get_started_button_click.setOnClickListener{
            findNavController().navigate(R.id.action_get_started_to_login_preview);
        }
        return view
    }

}