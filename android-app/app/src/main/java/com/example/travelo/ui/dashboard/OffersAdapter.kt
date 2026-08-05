package com.example.travelo.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.travelo.databinding.ItemOfferBinding
import com.example.travelo.model.MarketplaceOffer

class OffersAdapter(
    private var offers: List<MarketplaceOffer> = emptyList()
) : RecyclerView.Adapter<OffersAdapter.OfferViewHolder>() {

    fun submitList(newOffers: List<MarketplaceOffer>) {
        offers = newOffers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfferViewHolder {
        val binding = ItemOfferBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OfferViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OfferViewHolder, position: Int) {
        holder.bind(offers[position])
    }

    override fun getItemCount() = offers.size

    class OfferViewHolder(private val binding: ItemOfferBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(offer: MarketplaceOffer) {
            binding.offerName.text = offer.description
            binding.offerCost.text = "Cost: $${offer.price}" +
                    if (offer.openTime.isNotBlank())
                        "  ·  ${offer.openTime}–${offer.closeTime}"
                    else ""
            binding.offerTypeBadge.text = "BUSINESS"
        }
    }
}