package com.example.travelo.ui.route

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.travelo.databinding.ItemOfferSelectableBinding
import com.example.travelo.model.MarketplaceOffer
import com.google.android.material.color.MaterialColors

class SelectableOffersAdapter(
    private val onSelectionChanged: (Set<String>) -> Unit
) : RecyclerView.Adapter<SelectableOffersAdapter.ViewHolder>() {

    private var offers: List<MarketplaceOffer> = emptyList()
    private val selectedIds = mutableSetOf<String>()

    fun submitList(newOffers: List<MarketplaceOffer>, initialSelected: Set<String>) {
        offers = newOffers
        selectedIds.clear()
        selectedIds.addAll(initialSelected)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemOfferSelectableBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(offers[position], offers[position].proposalId in selectedIds)
    }

    override fun getItemCount() = offers.size

    inner class ViewHolder(private val binding: ItemOfferSelectableBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(offer: MarketplaceOffer, isSelected: Boolean) {
            binding.offerName.text = offer.description
            binding.offerCost.text = "Cost: $${offer.price}" +
                    if (offer.openTime.isNotBlank())
                        "  ·  ${offer.openTime}–${offer.closeTime}"
                    else ""
            binding.checkBox.isChecked = isSelected

            val bgColor = MaterialColors.getColor(
                binding.root.context,
                if (isSelected) com.google.android.material.R.attr.colorPrimaryContainer
                else com.google.android.material.R.attr.colorSurface,
                0
            )
            binding.root.setCardBackgroundColor(bgColor)

            binding.root.setOnClickListener {
                val id = offer.proposalId
                if (selectedIds.contains(id)) selectedIds.remove(id)
                else selectedIds.add(id)
                onSelectionChanged(selectedIds.toSet())
                notifyItemChanged(bindingAdapterPosition)
            }
        }
    }
}