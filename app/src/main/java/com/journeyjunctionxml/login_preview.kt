package com.journeyjunctionxml
import com.journeyjunctionxml.Firebase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.fragment.findNavController
class login_preview : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if(Firebase.getCurrentUser() != null) {
            findNavController().navigate(R.id.action_login_preview_to_home2);
        }
        val view = inflater.inflate(R.layout.login_preview, container, false)
        val sign_in: Button = view.findViewById<Button>(R.id.login_prev_sign_in)
        val sign_up: Button = view.findViewById<Button>(R.id.login_prev_sign_up)
        sign_in.setOnClickListener{
            findNavController().navigate(R.id.action_login_preview_to_sign_in)
        }
        sign_up.setOnClickListener {
            findNavController().navigate(R.id.action_login_preview_to_create_account)
        }
        return view
    }

}