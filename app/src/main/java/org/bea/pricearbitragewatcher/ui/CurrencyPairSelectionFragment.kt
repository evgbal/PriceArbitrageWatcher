package org.bea.pricearbitragewatcher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.bea.pricearbitragewatcher.databinding.FragmentCurrencyPairSelectionBinding

@AndroidEntryPoint
class CurrencyPairSelectionFragment : Fragment() {

    private val viewModel: CurrencyPairSelectionViewModel by viewModels()
    private var _binding: FragmentCurrencyPairSelectionBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: CurrencyPairAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCurrencyPairSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = CurrencyPairAdapter { pair, isSelected ->
            viewModel.togglePair(pair, isSelected)
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.searchEditText.addTextChangedListener { text ->
            viewModel.filterPairs(text.toString())
        }

        lifecycleScope.launch {
            viewModel.filteredPairs.collectLatest { pairs ->
                adapter.submitList(pairs)
            }
        }

        lifecycleScope.launch {
            viewModel.selectedPairs.collectLatest { selected ->
                adapter.updateSelectedPairs(selected)
            }
        }

        binding.selectAllButton.setOnClickListener {
            viewModel.selectAllFilteredPairs()
        }

        binding.deselectAllButton.setOnClickListener {
            viewModel.deselectAllFilteredPairs()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
