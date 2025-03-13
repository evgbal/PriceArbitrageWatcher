package org.bea.pricearbitragewatcher.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import org.bea.pricearbitragewatcher.databinding.ItemCurrencyPairBinding

class CurrencyPairAdapter(
    private val onToggle: (String, Boolean) -> Unit
) : ListAdapter<String, CurrencyPairAdapter.ViewHolder>(DiffCallback()) {

    private var selectedPairs: Set<String> = emptySet()

    class ViewHolder(private val binding: ItemCurrencyPairBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(pair: String, isSelected: Boolean, onToggle: (String, Boolean) -> Unit) {
            binding.pairName.text = pair
            binding.switchMaterial.setOnCheckedChangeListener(null)
            binding.switchMaterial.isChecked = isSelected
            binding.switchMaterial.setOnCheckedChangeListener { _, isChecked ->
                onToggle(pair, isChecked)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCurrencyPairBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pair = getItem(position)
        holder.bind(pair, selectedPairs.contains(pair), onToggle)
    }

    fun updateSelectedPairs(selected: Set<String>) {
        selectedPairs = selected
        notifyDataSetChanged()
    }

    class DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean = oldItem == newItem
    }
}