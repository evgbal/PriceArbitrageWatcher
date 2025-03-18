package org.bea.pricearbitragewatcher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.bea.pricearbitragewatcher.data.MarketPairsRepository
import org.bea.pricearbitragewatcher.data.SelectedPairRepository
import javax.inject.Inject

@HiltViewModel
open class CurrencyPairSelectionViewModel @Inject constructor(
    private val marketPairsRepository: MarketPairsRepository,
    private val selectedPairRepository: SelectedPairRepository
) : ViewModel() {

    private val _marketPairs = MutableStateFlow<List<String>>(emptyList())
    val marketPairs: StateFlow<List<String>> = _marketPairs.asStateFlow()

    private val _filteredPairs = MutableStateFlow<List<String>>(emptyList())
    val filteredPairs: StateFlow<List<String>> = _filteredPairs.asStateFlow()

    private val _selectedPairs = MutableStateFlow<Set<String>>(emptySet())
    val selectedPairs: StateFlow<Set<String>> = _selectedPairs.asStateFlow()

    init {
        viewModelScope.launch {
            launch {
                marketPairsRepository.getActiveMarketPairsFlow().collect { pairs ->
                    _marketPairs.value = pairs
                    _filteredPairs.value = pairs
                }
            }

            launch {
                selectedPairRepository.selectedPairs.collectLatest { pairs ->
                    _selectedPairs.value = pairs.toSet()
                }
            }
        }
    }

    fun togglePair(pair: String, isSelected: Boolean) {
        viewModelScope.launch {
            val updatedSet = _selectedPairs.value.toMutableSet()
            if (isSelected) updatedSet.add(pair) else updatedSet.remove(pair)
            _selectedPairs.value = updatedSet
            selectedPairRepository.savePairs(updatedSet.toList())
        }
    }

    fun filterPairs(query: String) {
        _filteredPairs.value = if (query.isEmpty()) {
            _marketPairs.value
        } else {
            _marketPairs.value.filter { it.contains(query, ignoreCase = true) }
        }
    }

    fun selectAllFilteredPairs() {
        viewModelScope.launch {
            val allFiltered = _filteredPairs.value.toSet()
            _selectedPairs.value = allFiltered
            selectedPairRepository.savePairs(allFiltered.toList())
        }
    }

    fun deselectAllFilteredPairs() {
        viewModelScope.launch {
            val currentSelected = _selectedPairs.value.toMutableSet()
            currentSelected.removeAll(_filteredPairs.value.toSet())
            _selectedPairs.value = currentSelected
            selectedPairRepository.savePairs(currentSelected.toList())
        }
    }
}