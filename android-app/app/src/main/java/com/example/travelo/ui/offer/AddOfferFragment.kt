package com.example.travelo.ui.offer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.travelo.databinding.FragmentAddOfferBinding
import com.example.travelo.network.CreateProposalRequest
import com.example.travelo.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.app.TimePickerDialog
import java.util.Locale

class AddOfferFragment : Fragment() {

    private var _binding: FragmentAddOfferBinding? = null
    private val binding get() = _binding!!
    private var startTimeValue: String = ""
    private var endTimeValue: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddOfferBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }
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
        binding.submitButton.text = if (loading) "" else "Submit to Marketplace"
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

    private fun onSubmit() {
        val name = binding.nameInput.text?.toString().orEmpty()
        val cost = binding.costInput.text?.toString().orEmpty()
        val lat = binding.latInput.text?.toString().orEmpty()
        val lon = binding.lonInput.text?.toString().orEmpty()
        val profit=binding.profitInput.text?.toString().orEmpty()

        if (name.isBlank() || cost.isBlank() || lat.isBlank() || lon.isBlank()||profit.isBlank()) {
            Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        setLoading(true)

        val request = CreateProposalRequest(
            businessId = "BUSINESS_123",
            tripId = "GLOBAL_MARKETPLACE",
            description = name,
            price = cost.toDoubleOrNull() ?: 0.0,
            lat = lat.toDoubleOrNull() ?: 0.0,
            lng = lon.toDoubleOrNull() ?: 0.0,
            location = "$lat,$lon",
            openTime = startTimeValue,
            closeTime = endTimeValue,
            durationMinutes = binding.durationInput.text?.toString()?.toDoubleOrNull() ?: 0.0,
            profit= profit.toDoubleOrNull() ?: 15.0
        )

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.createProposal(request)
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    if (response.isSuccessful) {
                        Toast.makeText(requireContext(), "Offer published!", Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    } else {
                        Toast.makeText(requireContext(), "Failed to publish", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}