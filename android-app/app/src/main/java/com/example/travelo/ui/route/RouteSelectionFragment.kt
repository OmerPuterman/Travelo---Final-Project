package com.example.travelo.ui.route

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelo.R
import com.example.travelo.databinding.FragmentRouteSelectionBinding
import com.example.travelo.model.MarketplaceOffer
import com.example.travelo.network.RetrofitInstance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RouteSelectionFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentRouteSelectionBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var offers: List<MarketplaceOffer> = emptyList()
    private var selectedIds = mutableSetOf<String>()
    private lateinit var adapter: SelectableOffersAdapter

    private val tripId: String by lazy {
        arguments?.getString("tripId") ?: "GLOBAL_MARKETPLACE"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouteSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        adapter = SelectableOffersAdapter { newSelectedIds ->
            selectedIds = newSelectedIds.toMutableSet()
            updateGenerateButton()
            updateMapMarkers()
        }

        binding.offersList.layoutManager = LinearLayoutManager(requireContext())
        binding.offersList.adapter = adapter

        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)

        binding.generateButton.setOnClickListener { onGenerateRoute() }

        loadOffers()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = false
        updateMapMarkers()
    }

    private fun loadOffers() {
        binding.loadingIndicator.visibility = View.VISIBLE
        binding.emptyText.visibility = View.GONE
        binding.offersList.visibility = View.GONE
        binding.mapContainer.visibility = View.GONE
        binding.sectionLabel.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getProposalsForTrip("GLOBAL_MARKETPLACE")
                withContext(Dispatchers.Main) {
                    binding.loadingIndicator.visibility = View.GONE
                    if (response.isSuccessful) {
                        offers = response.body() ?: emptyList()
                        if (offers.isEmpty()) {
                            binding.emptyText.visibility = View.VISIBLE
                        } else {
                            selectedIds = offers.map { it.proposalId }.toMutableSet()
                            adapter.submitList(offers, selectedIds)
                            binding.offersList.visibility = View.VISIBLE
                            binding.mapContainer.visibility = View.VISIBLE
                            binding.sectionLabel.visibility = View.VISIBLE
                            updateGenerateButton()
                            updateMapMarkers()
                        }
                    } else {
                        Log.e("TRAVELO", "Server Error: ${response.code()}")
                        binding.emptyText.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.loadingIndicator.visibility = View.GONE
                    Log.e("TRAVELO", "Network Exception: ${e.message}")
                    Toast.makeText(requireContext(), "Failed to load offers", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun updateMapMarkers() {
        val map = googleMap ?: return
        if (offers.isEmpty()) return

        map.clear()
        val boundsBuilder = LatLngBounds.Builder()
        var hasValidLocation = false

        offers.forEach { offer ->
            val latLng = if (offer.lat != 0.0 || offer.lng != 0.0)
                LatLng(offer.lat, offer.lng)
            else
                parseLatLng(offer.location) ?: return@forEach
                hasValidLocation = true
            val isSelected = offer.proposalId in selectedIds
            map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(offer.description)
                    .snippet("$${offer.price}")
                    .icon(
                        BitmapDescriptorFactory.defaultMarker(
                            if (isSelected) BitmapDescriptorFactory.HUE_GREEN
                            else BitmapDescriptorFactory.HUE_RED
                        )
                    )
            )
            boundsBuilder.include(latLng)
        }

        if (hasValidLocation) {
            try {
                val bounds = boundsBuilder.build()
                map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
            } catch (e: Exception) {
                Log.e("TRAVELO", "Camera update failed: ${e.message}")
            }
        }
    }

    private fun updateGenerateButton() {
        binding.generateButton.text = "Generate Route (${selectedIds.size} Selected)"
        binding.generateButton.isEnabled = selectedIds.isNotEmpty()
    }

    private fun onGenerateRoute() {
        if (selectedIds.isEmpty()) {
            Toast.makeText(requireContext(), "Select at least one place!", Toast.LENGTH_SHORT).show()
            return
        }

        binding.generateButton.isEnabled = false
        binding.generateButton.text = ""
        binding.generatingIndicator.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.generateLiveRoute(tripId, selectedIds.toList())
                withContext(Dispatchers.Main) {
                    binding.generatingIndicator.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(requireContext(), "Route Optimized!", Toast.LENGTH_SHORT).show()
                        findNavController().navigate(
                            R.id.action_routeSelection_to_tripDetails,
                            bundleOf("tripId" to tripId)
                        )
                    } else {
                        binding.generateButton.isEnabled = true
                        updateGenerateButton()
                        Toast.makeText(requireContext(), "Optimization failed. Check constraints.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.generatingIndicator.visibility = View.GONE
                    binding.generateButton.isEnabled = true
                    updateGenerateButton()
                    Toast.makeText(requireContext(), "Server Error", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun parseLatLng(raw: String): LatLng? {
        val parts = raw.split(",").map { it.trim() }
        if (parts.size != 2) return null
        val lat = parts[0].toDoubleOrNull() ?: return null
        val lon = parts[1].toDoubleOrNull() ?: return null
        return LatLng(lat, lon)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}