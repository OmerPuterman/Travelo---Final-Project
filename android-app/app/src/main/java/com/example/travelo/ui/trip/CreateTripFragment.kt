package com.example.travelo.ui.trip

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.travelo.databinding.FragmentCreateTripBinding
import com.example.travelo.model.Trip
import com.example.travelo.network.RetrofitInstance
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class CreateTripFragment : Fragment() {

    private var _binding: FragmentCreateTripBinding? = null
    private val binding get() = _binding!!
    private var startTimeValue: String = ""
    private var endTimeValue: String = ""
    private val generatedTripCode = "TRIP-${(1000..9999).random()}"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.title = "Plan: $generatedTripCode"
        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        binding.destinationInput.setText("Paris Group Tour")
        binding.budgetInput.setText("200")
        binding.maxTimeInput.setText("480")
        binding.startLocInput.setText("48.8606, 2.3376")
        binding.endLocInput.setText("49.0097, 2.5479")

        binding.submitButton.setOnClickListener { onSubmit() }

        binding.startTimeInput.setOnClickListener {
            showTimePicker { time ->
                startTimeValue = time
                binding.startTimeInput.setText(time)
            }
        }

        binding.endTimeInput.setOnClickListener {
            showTimePicker { time ->
                endTimeValue = time
                binding.endTimeInput.setText(time)
            }
        }
    }

    private fun setLoading(loading: Boolean) {
        binding.submitButton.isEnabled = !loading
        binding.loadingIndicator.visibility = if (loading) View.VISIBLE else View.GONE
        binding.submitButton.text = if (loading) "" else "Save Trip Constraints"
    }

    private fun onSubmit() {
        setLoading(true)

        // newTrip must be built HERE, outside the coroutine
        val newTrip = Trip(
            tripId = generatedTripCode,
            destination = binding.destinationInput.text?.toString().orEmpty(),
            budget = binding.budgetInput.text?.toString()?.toDoubleOrNull() ?: 500.0,
            maxTimeMinutes = binding.maxTimeInput.text?.toString()?.toIntOrNull() ?: 720,
            startLocation = binding.startLocInput.text?.toString().orEmpty(),
            endLocation = binding.endLocInput.text?.toString().orEmpty(),
            startDate = "TBD",
            guideId = "GUIDE",
            startTime = startTimeValue,
            numberOfTravelers = 1
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.createTrip(newTrip) // now in scope
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    if (response.isSuccessful) showSuccessDialog()
                    else Toast.makeText(requireContext(), "Server Error: ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Trip Created!")
            .setMessage("Your unique Trip Code is:\n\n$generatedTripCode\n\nSave this code to manage the trip, and share it with your travelers so they can view the itinerary!")
            .setCancelable(false)
            .setPositiveButton("Got it!") { _, _ -> findNavController().popBackStack() }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showTimePicker(onTimeSelected: (String) -> Unit) {
        val picker = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val time = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                onTimeSelected(time)
            },
            9,      // default hour
            0,      // default minute
            true    // true = 24-hour format, false = AM/PM
        )

        picker.show()
    }
}