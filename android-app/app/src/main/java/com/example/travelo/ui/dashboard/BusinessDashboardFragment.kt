package com.example.travelo.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelo.R
import com.example.travelo.databinding.FragmentBusinessDashboardBinding
import com.example.travelo.model.MarketplaceOffer
import com.example.travelo.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BusinessDashboardFragment : Fragment() {

    private var _binding: FragmentBusinessDashboardBinding? = null
    private val binding get() = _binding!!
    private val adapter = OffersAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBusinessDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.offersList.layoutManager = LinearLayoutManager(requireContext())
        binding.offersList.adapter = adapter

        binding.fabAddOffer.setOnClickListener {
            findNavController().navigate(R.id.action_businessDashboard_to_addOffer)
        }

        binding.logoutButton.setOnClickListener {
            findNavController().navigate(R.id.action_businessDashboard_to_login)
        }

        loadOffers()
    }

    private fun loadOffers() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE
        binding.offersList.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getProposalsForTrip("GLOBAL_MARKETPLACE")
                withContext(Dispatchers.Main) {
                    binding.loadingIndicator.visibility = View.GONE
                    if (response.isSuccessful) {
                        val offers: List<MarketplaceOffer> = response.body() ?: emptyList()
                        if (offers.isEmpty()) {
                            binding.emptyText.visibility = View.VISIBLE
                        } else {
                            adapter.submitList(offers)
                            binding.offersList.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.loadingIndicator.visibility = View.GONE
                    Toast.makeText(requireContext(), "Failed to load live offers", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}