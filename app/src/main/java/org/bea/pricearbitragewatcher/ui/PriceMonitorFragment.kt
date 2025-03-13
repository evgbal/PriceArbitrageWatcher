package org.bea.pricearbitragewatcher.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
//import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.bea.pricearbitragewatcher.databinding.FragmentPriceMonitorBinding

@AndroidEntryPoint
class PriceMonitorFragment : Fragment() {
    private val viewModel: PriceMonitorViewModel by viewModels()
    private var _binding: FragmentPriceMonitorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPriceMonitorBinding.inflate(
            inflater, container, false
        )
        return binding.root
    }

    //
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {

            // Выполняем подписку
            viewModel.subscribeToGateIo()

//            // Наблюдаем за состоянием через collect
//            lifecycleScope.launch {
//                viewModel.status.collect { status ->
//                    if (status.isNotEmpty()) {
//                        Toast.makeText(requireContext(), status, Toast.LENGTH_SHORT).show()
//                    }
//                }
//            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.statusGateIo.collect { message ->
                        // Обновляем UI
                        binding.textViewGateIo.text = "GateIo: ${message}"
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            binding.textViewGateIo.text = "GateIo: ${e.message}"
        }



        try {

            // Выполняем подписку
            viewModel.subscribeToCoinEx()

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.statusCoinEx.collect { message ->
                        // Обновляем UI
                        binding.textViewCoinEx.text = "CoinEx: ${message}"
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            binding.textViewCoinEx.text = "CoinEx: ${e.message}"
        }


        try {

            // Выполняем подписку
            viewModel.subscribeToHuobi()

            viewLifecycleOwner.lifecycleScope.launch {
                viewLifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    viewModel.statusHuobi.collect { message ->
                        // Обновляем UI
                        binding.textViewHuobi.text = "Huobi: ${message}"
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
            binding.textViewHuobi.text = "Huobi: ${e.message}"
        }

    }


}
