package org.bea.pricearbitragewatcher.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bea.pricearbitragewatcher.R

@AndroidEntryPoint
class ExchangeArbitrageFragment : Fragment(R.layout.fragment_exchange_arbitrage) {

    private val viewModel: EchangeArbitrageViewModel by viewModels()
    private val adapter = ExchangeArbitrageAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        collectArbitrageRoutes()
    }

    private fun collectArbitrageRoutes() {
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.arbitrageRoutes.collect { routes ->
                    withContext(Dispatchers.Main) {
                        adapter.submitList(routes)
                    }
                }
            }
        }
    }
}