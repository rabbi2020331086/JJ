package com.journeyjunctionxml
import com.journeyjunctionxml.Firebase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class create_account : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_account, container, false)
        val passwordLayout: TextInputLayout = view.findViewById(R.id.create_account_password_layout)
        val signupButton: Button = view.findViewById(R.id.sign_up)
        signupButton.setOnClickListener {
            val emailTextInputEditText: TextInputEditText = view.findViewById(R.id.create_account_email_edit)
            val passwordEditText: TextInputEditText = passwordLayout.findViewById(R.id.create_account_password_edit)
            val nameedit: TextInputEditText = view.findViewById(R.id.create_account_name_edit)
            val retypepassedit: TextInputEditText = view.findViewById(R.id.create_account_retype_password)
            val password = passwordEditText.text.toString()
            val email = emailTextInputEditText.text.toString()
            val name = nameedit.text.toString()
            val retypepass = retypepassedit.text.toString()
            val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
            val adventurerRadioButton = view.findViewById<RadioButton>(R.id.radio_adventurer)
            var type = if (adventurerRadioButton.isChecked) {
                "adventurer"
            } else {
                "explorer"
            }
            radioGroup.setOnCheckedChangeListener { _, checkedId ->
                type = if (checkedId == R.id.radio_adventurer) {
                    "adventurer"
                } else {
                    "explorer"
                }
            }
            if(email.isEmpty() || password.isEmpty() || name.isEmpty() || retypepass.isEmpty()) {
                Toast.makeText(requireContext(), "All field must be filled!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password != retypepass){
                Toast.makeText(requireContext(), "Password did not matched!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(password.length <6){
                Toast.makeText(requireContext(), "Password must contain at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Firebase.createAccount(email,password,name,type,
                onSuccess = {
                    sendEmailVerification(onComplete = {istrue ->
                        Toast.makeText(requireContext(), "Please verify your email..", Toast.LENGTH_SHORT).show()
                    })
                    findNavController().navigate(R.id.action_create_account_to_home2)
                },
                onFailure = {Exception ->
                    Toast.makeText(requireContext(), "Failed to create account: ${Exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
        return view
    }
    fun sendEmailVerification(onComplete: (Boolean) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        currentUser?.sendEmailVerification()?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true)
            } else {
                onComplete(false)
            }
        }
    }
}