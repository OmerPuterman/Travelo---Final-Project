package com.example.travelo.ui.itinerary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.travelo.R
import com.example.travelo.databinding.FragmentTravelerItineraryBinding
import com.example.travelo.model.Route
import com.example.travelo.network.RetrofitInstance
import com.example.travelo.ui.trip.RouteStopsAdapter
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.color.MaterialColors
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TravelerItineraryFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentTravelerItineraryBinding? = null
    private val binding get() = _binding!!

    private var googleMap: GoogleMap? = null
    private var pendingRoute: Route? = null
    private lateinit var adapter: RouteStopsAdapter

    private val tripId: String by lazy {
        arguments?.getString("tripId") ?: "0"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTravelerItineraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        val badgeColor = MaterialColors.getColor(
            requireContext(), com.google.android.material.R.attr.colorTertiary, 0
        )
        adapter = RouteStopsAdapter(badgeColor)
        binding.stopsList.layoutManager = LinearLayoutManager(requireContext())
        binding.stopsList.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        binding.stopsList.adapter = adapter

        val mapFragment = SupportMapFragment.newInstance()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commit()
        mapFragment.getMapAsync(this)

        if (isTripConfirmed()) {
            binding.acceptButton.isEnabled = false
            binding.acceptButton.text = "✓ Trip Confirmed!"
            binding.acceptButton.icon = null
        }

        binding.acceptButton.setOnClickListener {
            markTripConfirmed()
            binding.acceptButton.isEnabled = false
            binding.acceptButton.text = "✓ Trip Confirmed!"
            binding.acceptButton.icon = null
            Toast.makeText(requireContext(), "Trip confirmed! Saved to your trips.", Toast.LENGTH_SHORT).show()
        }

        loadRoute()
    }

    // =========================================================
    // Locally-saved trip confirmations (per device, per account)
    // =========================================================
    // NOTE: there's no travelerIds field on Trip/User in the backend yet, so this
    // can't be tied to your account across devices without a backend change.
    // This persists on-device via SharedPreferences, scoped by Firebase uid so
    // different accounts on the same device/emulator don't share state.

    private fun confirmedTripsPrefs() =
        requireContext().getSharedPreferences("travelo_prefs", android.content.Context.MODE_PRIVATE)

    private fun confirmedTripsKey(): String {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        return "confirmed_trips_$uid"
    }

    private fun isTripConfirmed(): Boolean {
        val saved = confirmedTripsPrefs().getStringSet(confirmedTripsKey(), emptySet())
        return saved?.contains(tripId) == true
    }

    private fun markTripConfirmed() {
        val prefs = confirmedTripsPrefs()
        val key = confirmedTripsKey()
        val updated = (prefs.getStringSet(key, emptySet()) ?: emptySet()).toMutableSet()
        updated.add(tripId)
        prefs.edit().putStringSet(key, updated).apply()
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMapToolbarEnabled = false
        pendingRoute?.let { drawRoute(it) }
    }

    private fun loadRoute() {
        binding.loadingIndicator.visibility = View.VISIBLE

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.getRouteForTrip(tripId)
                withContext(Dispatchers.Main) {
                    binding.loadingIndicator.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        showRoute(response.body()!!)
                    } else {
                        binding.emptyText.visibility = View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.loadingIndicator.visibility = View.GONE
                    binding.emptyText.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Network error fetching itinerary.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showRoute(route: Route) {
        binding.mapContainer.visibility = View.VISIBLE
        binding.stopsList.visibility = View.VISIBLE
        binding.bottomBar.visibility = View.VISIBLE

        adapter.submitList(route.stops)

        pendingRoute = route
        googleMap?.let { drawRoute(route) }
    }

    private fun drawRoute(route: Route) {
        val map = googleMap ?: return
        val sorted = route.stops.sortedBy { it.order }
        val points = sorted
            .filter { it.lat != 0.0 || it.lon != 0.0 }
            .map { LatLng(it.lat, it.lon) }

        if (points.isEmpty()) return
        map.clear()

        val tertiaryColor = MaterialColors.getColor(
            requireContext(), com.google.android.material.R.attr.colorTertiary, 0
        )

        if (points.size >= 2) {
            map.addPolyline(
                PolylineOptions().addAll(points).width(8f).color(tertiaryColor)
            )
        }

        sorted.filter { it.lat != 0.0 || it.lon != 0.0 }.forEach { stop ->
            map.addMarker(
                MarkerOptions()
                    .position(LatLng(stop.lat, stop.lon))
                    .title("${stop.order}. ${stop.description}")
                    .snippet(stop.arrivalTime)
            )
        }

        try {
            val bounds = LatLngBounds.Builder().apply { points.forEach { include(it) } }.build()
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 120))
        } catch (e: Exception) { /* map not laid out yet */ }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}