package com.journeyjunctionxml
import com.journeyjunctionxml.Firebase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController

class get_started : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(Firebase.getCurrentUser() != null) {
            findNavController().navigate(R.id.action_get_started_to_home2);
        }
        val view =  inflater.inflate(R.layout.get_started, container, false)
        val get_started_button_click = view.findViewById<Button>(R.id.get_started_button)
        get_started_button_click.setOnClickListener{
            findNavController().navigate(R.id.action_get_started_to_login_preview);
        }
        return view
    }

}