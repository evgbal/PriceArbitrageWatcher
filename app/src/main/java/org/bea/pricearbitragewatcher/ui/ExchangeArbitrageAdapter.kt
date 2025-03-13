package org.bea.pricearbitragewatcher.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.bea.pricearbitragewatcher.R
import org.bea.pricearbitragewatcher.data.ArbitrageRoute

class ExchangeArbitrageAdapter : ListAdapter<ArbitrageRoute, ExchangeArbitrageAdapter.ArbitrageViewHolder>(
    object : DiffUtil.ItemCallback<ArbitrageRoute>() {
        override fun areItemsTheSame(oldItem: ArbitrageRoute, newItem: ArbitrageRoute) =
            oldItem.startExchange == newItem.startExchange &&
                    oldItem.endExchange == newItem.endExchange &&
                    oldItem.baseSymbol == newItem.baseSymbol &&
                    oldItem.quoteSymbol == newItem.quoteSymbol

        override fun areContentsTheSame(oldItem: ArbitrageRoute, newItem: ArbitrageRoute) =
            oldItem == newItem
    }
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ArbitrageViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_exchange_arbitrage, parent, false)
        )

    override fun onBindViewHolder(holder: ArbitrageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ArbitrageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(route: ArbitrageRoute) {
            itemView.findViewById<TextView>(R.id.textArbitrage).text =
                buildString {
                    append("${route.baseSymbol}/${route.quoteSymbol} | ")
                    append("${route.startExchange} -> ${route.endExchange} | ")
                    append("Profit: ${"%.2f".format(route.profitPercentage)}%")
                }
        }
    }
}
