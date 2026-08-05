package com.example.travelo.ui.trip

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
import com.example.travelo.databinding.FragmentTripDetailsBinding
import com.example.travelo.model.Route
import com.example.travelo.model.Trip
import com.example.travelo.network.RetrofitInstance
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TripDetailsFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentTripDetailsBinding? = null
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
        _binding = FragmentTripDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

        val badgeColor = MaterialColors.getColor(
            requireContext(), com.google.android.material.R.attr.colorPrimary, 0
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

        loadRoute()
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
                val routeResponse = RetrofitInstance.api.getRouteForTrip(tripId)
                val trip: Trip? = try {
                    val tripResponse = RetrofitInstance.api.getTrip(tripId)
                    if (tripResponse.isSuccessful) tripResponse.body() else null
                } catch (e: Exception) {
                    null // Trip details are a bonus; don't fail the whole screen over them.
                }

                withContext(Dispatchers.Main) {
                    binding.loadingIndicator.visibility = View.GONE
                    if (routeResponse.isSuccessful && routeResponse.body() != null) {
                        showRoute(routeResponse.body()!!, trip)
                    } else {
                        binding.emptyText.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "No route found. Generate one first.", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.loadingIndicator.visibility = View.GONE
                    binding.emptyText.visibility = View.VISIBLE
                    Toast.makeText(requireContext(), "Network error.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showRoute(route: Route, trip: Trip?) {
        binding.summaryCard.visibility = View.VISIBLE
        binding.mapContainer.visibility = View.VISIBLE
        binding.stopsList.visibility = View.VISIBLE

        bindDetails(route, trip)

        adapter.submitList(route.stops)

        pendingRoute = route
        googleMap?.let { drawRoute(route) }
    }

    private fun bindDetails(route: Route, trip: Trip?) {
        binding.detailDestination.text = trip?.destination ?: "Final Itinerary"

        val stopCount = route.stops.size
        val subtitleParts = mutableListOf<String>()
        trip?.startDate?.takeIf { it.isNotBlank() && it != "TBD" }?.let { subtitleParts.add(it) }
        subtitleParts.add("$stopCount ${if (stopCount == 1) "stop" else "stops"}")
        binding.detailSubtitle.text = subtitleParts.joinToString(" · ")

        binding.totalCost.text = "$${route.totalCost}"
        binding.totalTime.text = "${route.totalTime.toInt()} min"
        binding.totalStops.text = stopCount.toString()

        binding.detailBudget.text = trip?.budget?.let { "$${it}" } ?: "—"
        binding.detailTravelers.text = trip?.numberOfTravelers?.toString() ?: "—"
        binding.detailStartTime.text = trip?.startTime?.takeIf { it.isNotBlank() } ?: "—"
    }

    private fun drawRoute(route: Route) {
        val map = googleMap ?: return
        val sorted = route.stops.sortedBy { it.order }
        val points = sorted
            .filter { it.lat != 0.0 || it.lon != 0.0 }
            .map { LatLng(it.lat, it.lon) }

        if (points.isEmpty()) return
        map.clear()

        val primaryColor = MaterialColors.getColor(
            requireContext(), com.google.android.material.R.attr.colorPrimary, 0
        )

        if (points.size >= 2) {
            map.addPolyline(
                PolylineOptions().addAll(points).width(8f).color(primaryColor)
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