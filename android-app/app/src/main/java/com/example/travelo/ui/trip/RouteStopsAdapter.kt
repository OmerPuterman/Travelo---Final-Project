package com.example.travelo.ui.trip

import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.travelo.databinding.ItemRouteStopBinding
import com.example.travelo.model.Route

class RouteStopsAdapter(
    private val badgeColor: Int
) : RecyclerView.Adapter<RouteStopsAdapter.ViewHolder>() {

    private var stops: List<Route.Stop> = emptyList()

    fun submitList(newStops: List<Route.Stop>) {
        stops = newStops.sortedBy { it.order }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRouteStopBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stops[position])
    }

    override fun getItemCount() = stops.size

    inner class ViewHolder(private val binding: ItemRouteStopBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(stop: Route.Stop) {
            binding.stopNumber.text = "${stop.order}"
            binding.stopDescription.text = stop.description
            binding.stopArrivalTime.text = stop.arrivalTime

            val circle = GradientDrawable()
            circle.shape = GradientDrawable.OVAL
            circle.setColor(badgeColor)
            binding.stopNumber.background = circle
        }
    }
}