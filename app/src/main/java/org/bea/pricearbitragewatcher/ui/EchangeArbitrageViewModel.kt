package org.bea.pricearbitragewatcher.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.bea.pricearbitragewatcher.data.ArbitrageRoute
import org.bea.pricearbitragewatcher.data.WebSocketTickerRepository
import javax.inject.Inject

@HiltViewModel
class EchangeArbitrageViewModel @Inject constructor(
    private val repository: WebSocketTickerRepository
) : ViewModel() {

    val arbitrageRoutes: Flow<List<ArbitrageRoute>> = repository.arbitrageRoutes
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}