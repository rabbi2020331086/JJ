package com.journeyjunctionxml

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import java.text.SimpleDateFormat
import java.util.Locale

class create_journey : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view =  inflater.inflate(R.layout.create_journey, container, false)

        val start_time_button = view.findViewById<View>(R.id.create_trip_start_time)
        val end_time_button = view.findViewById<View>(R.id.create_trip_end_time)
        val createTripTitleEditText = view.findViewById<EditText>(R.id.create_trip_title)
        val createTripDestinationEditText = view.findViewById<EditText>(R.id.create_trip_destinaniton)
        val createJourneyDescriptionEditText = view.findViewById<EditText>(R.id.create_journey_description)
        val createTripCheckpointsEditText = view.findViewById<EditText>(R.id.create_trip_checkpints)
        val createTripBudgetEditText = view.findViewById<EditText>(R.id.create_trip_budget)
        val createTripCapacityEditText = view.findViewById<EditText>(R.id.create_trip_capacity)
        val maleCheckBox = view.findViewById<CheckBox>(R.id.maleCheckBox)
        val femaleCheckBox = view.findViewById<CheckBox>(R.id.femaleCheckBox)
        val createTripsDoneButton = view.findViewById<Button>(R.id.create_trips_done)
        val createTripsDiscardButton = view.findViewById<Button>(R.id.create_trips_discard)
        var start_date = "-1"
        var start_time = "-1"
        var end_time = "-1"
        var end_date = "-1"
        createTripsDoneButton.setOnClickListener {
            val tripTitle = createTripTitleEditText.text.toString()
            val tripDestination = createTripDestinationEditText.text.toString()
            val journeyDescription = createJourneyDescriptionEditText.text.toString()
            val tripCheckpoints = createTripCheckpointsEditText.text.toString()
            val tripBudget = createTripBudgetEditText.text.toString()
            val tripCapacity = createTripCapacityEditText.text.toString()
            if (tripTitle.isEmpty() || tripDestination.isEmpty() || journeyDescription.isEmpty() || tripCheckpoints.isEmpty() || tripBudget.isEmpty() || tripCapacity.isEmpty()) {
                Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!maleCheckBox.isChecked && !femaleCheckBox.isChecked) {
                Toast.makeText(context, "Select at least one gender", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(start_time == "-1" || start_date == "-1" || end_date == "-1" || end_time == "-1"){
                Toast.makeText(context, "Start and End time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener

            }
            val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val startDateAndTime = formatter.parse("$start_date $start_time")
            val endDateAndTime = formatter.parse("$end_date $end_time")
            if (endDateAndTime.before(startDateAndTime)) {
                Toast.makeText(requireContext(), "End date and time must be later than start date and time", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val isValidNumber = tripBudget.toIntOrNull() != null
            val budget = tripBudget.toInt()
            val isvalidCapacity = tripCapacity.toIntOrNull() != null
            val capacity = tripCapacity.toInt()
            if(!isvalidCapacity){
                Toast.makeText(requireContext(), "Please enter valid number of member", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!isValidNumber) {
                Toast.makeText(requireContext(), "Please enter valid budget", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(budget < 0){
                Toast.makeText(requireContext(), "Budget cannot be negative number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if(capacity <= 1){
                Toast.makeText(requireContext(), "Member must be at least 1", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            var gender: String
            if(maleCheckBox.isChecked && femaleCheckBox.isChecked){
                gender = "Male and Female"
            }
            else if(maleCheckBox.isChecked){
                gender = "Male"
            }
            else {
                gender = "Female"
            }
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val startDateString = dateFormat.format(startDateAndTime)
            val endDateString = dateFormat.format(endDateAndTime)
            val tripDetailsList = listOf(
                tripTitle,
                tripDestination,
                startDateString,
                endDateString,
                journeyDescription,
                tripCheckpoints,
                tripBudget,
                tripCapacity,
                gender
            )
            Firebase.create_journey(tripDetailsList) { journeyID ->
                if (journeyID != null) {
                    Log.d(TAG, "Journey created successfully with ID: $journeyID")
                    Firebase.add_journeyID_to_users_collection(journeyID){
                        flag ->
                            if(flag){
                                Toast.makeText(requireContext(), "Journey created successfully", Toast.LENGTH_SHORT).show()
                                findNavController().navigate(R.id.action_create_journey_to_home2)
                            }
                        else{
                                Toast.makeText(requireContext(), "Failed to create the Journey", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to create the Journey", Toast.LENGTH_SHORT).show()
                }
            }

        }
        createTripsDiscardButton.setOnClickListener {
            Toast.makeText(requireContext(), "Journey Discarded", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.action_create_journey_to_home2)

        }
        start_time_button.setOnClickListener {
            showDatePickerDialog { year, month, dayOfMonth ->
                start_date = "$dayOfMonth/${month + 1}/$year"
                showTimePickerDialog { hourOfDay, minute ->
                    start_time = "$hourOfDay:$minute"
                    (start_time_button as Button).text = "$start_date - $start_time"
                }
            }
        }
        end_time_button.setOnClickListener {
            showDatePickerDialog { year, month, dayOfMonth ->
                end_date = "$dayOfMonth/${month + 1}/$year"
                Toast.makeText(requireContext(), "Selected Date: $end_date", Toast.LENGTH_SHORT).show()
                showTimePickerDialog { hourOfDay, minute ->
                    end_time = "$hourOfDay:$minute"
                    (end_time_button as Button).text = "$end_date - $end_time"
                    Toast.makeText(requireContext(), "Selected Time: $end_time", Toast.LENGTH_SHORT).show()
                }
            }

        }
        return view
    }
    private fun showDatePickerDialog(function: (Int, Int, Int) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                function(year, monthOfYear, dayOfMonth)
            },
            calendar.get(java.util.Calendar.YEAR),
            calendar.get(java.util.Calendar.MONTH),
            calendar.get(java.util.Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    private fun showTimePickerDialog(function: (Int, Int) -> Unit) {
        val calendar = java.util.Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { view, hourOfDay, minute ->
                function(hourOfDay, minute)
            },
            calendar.get(java.util.Calendar.HOUR_OF_DAY),
            calendar.get(java.util.Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }
}