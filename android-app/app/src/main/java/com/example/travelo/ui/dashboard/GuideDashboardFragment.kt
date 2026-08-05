package com.example.travelo.ui.dashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.travelo.R
import com.example.travelo.databinding.FragmentGuideDashboardBinding
import com.example.travelo.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GuideDashboardFragment : Fragment() {

    private var _binding: FragmentGuideDashboardBinding? = null
    private val binding get() = _binding!!

    private var previewJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGuideDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.fabNewTrip.setOnClickListener {
            findNavController().navigate(R.id.action_guideDashboard_to_createTrip)
        }
        binding.logoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_guideDashboard_to_login)
        }

        binding.tripCodeInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.manageButton.isEnabled = !s.isNullOrBlank()
                onCodeChanged(s?.toString().orEmpty())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        binding.manageButton.setOnClickListener {
            val raw = binding.tripCodeInput.text?.toString()?.uppercase().orEmpty()
            if (raw.isBlank()) return@setOnClickListener
            val finalCode = if (raw.startsWith("TRIP-")) raw else "TRIP-$raw"
            findNavController().navigate(
                R.id.action_guideDashboard_to_routeSelection,
                bundleOf("tripId" to finalCode)
            )
        }
    }

    private fun normalizeCode(raw: String): String {
        val upper = raw.uppercase().trim()
        return if (upper.startsWith("TRIP-")) upper else "TRIP-$upper"
    }

    // Debounced live preview: once a full TRIP-#### code is entered, fetch and show it.
    private fun onCodeChanged(raw: String) {
        previewJob?.cancel()
        val code = normalizeCode(raw)
        if (!Regex("^TRIP-\\d{4}$").matches(code)) {
            binding.previewCard.visibility = View.GONE
            return
        }

        previewJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            delay(350) // wait for typing to settle before hitting the network
            try {
                val response = RetrofitInstance.api.getTrip(code)
                withContext(Dispatchers.Main) {
                    val trip = if (response.isSuccessful) response.body() else null
                    if (trip != null) {
                        binding.previewDestination.text = trip.destination
                        binding.previewBudget.text = "$${trip.budget}"
                        binding.previewTravelers.text = trip.numberOfTravelers.toString()
                        binding.previewStartTime.text =
                            trip.startTime.takeIf { it.isNotBlank() } ?: "—"
                        binding.previewCard.visibility = View.VISIBLE
                    } else {
                        binding.previewCard.visibility = View.GONE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { binding.previewCard.visibility = View.GONE }
            }
        }
    }

    override fun onDestroyView() {
        previewJob?.cancel()
        super.onDestroyView()
        _binding = null
    }
}