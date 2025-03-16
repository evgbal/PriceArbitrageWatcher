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
import org.bea.pricearbitragewatcher.databinding.ItemExchangeArbitrageBinding

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
            ItemExchangeArbitrageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

    override fun onBindViewHolder(holder: ArbitrageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ArbitrageViewHolder(private val binding: ItemExchangeArbitrageBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(route: ArbitrageRoute) {
            // Заполняем текст с валютами
            "${route.baseSymbol}/${route.quoteSymbol}".also { binding.textSymbol.text = it }

            // Заполняем текст с ценой покупки (Bid)
            "Buy: ${"%.8f".format(route.startBid)}".also { binding.textBuy.text = it }

            // Заполняем текст с ценой продажи (Ask)
            "Sell: ${"%.8f".format(route.endAsk)}".also { binding.textSell.text = it }

            // Заполняем текст с профитом
            "${"%.2f".format(route.profitPercentage)}%".also { binding.textProfit.text = it }

            // Привязка логотипов бирж
            binding.imageStartExchange.setImageResource(getExchangeLogo(route.startExchange))
            binding.imageEndExchange.setImageResource(getExchangeLogo(route.endExchange))
        }

        private fun getExchangeLogo(exchangeName: String): Int {
            return when (exchangeName) {
                "Gate.io" -> R.drawable.gate_io_logo_no_text_512
                "Huobi" -> R.drawable.huobi_logo_no_text_512
                "CoinEx" -> R.drawable.coin_ex_logo_no_text_512
                else -> android.R.color.transparent
            }
        }

//        fun bind(route: ArbitrageRoute) {
//            binding.textArbitrage.text = buildString {
//                append("${route.baseSymbol}/${route.quoteSymbol} | ")
//                append("${route.startExchange} -> ${route.endExchange} | ")
//                append("Profit: ${"%.2f".format(route.profitPercentage)}%")
//            }
//        }
    }
}
