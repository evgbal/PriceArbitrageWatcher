package org.bea.pricearbitragewatcher.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectedPairRepository @Inject constructor(
    private val selectedPairDao: SelectedPairDao,
    private val currencyPairDao: CurrencyPairDao
) {
    private val defaultPairs = listOf("BTC/USDT", "LTC/USDT", "ETH/USDT", "ETHW/USDT")
    private val _selectedPairs = MutableStateFlow(defaultPairs)
    val selectedPairs: Flow<List<String>> = _selectedPairs.asStateFlow()

    suspend fun loadPairs() {
        val savedPairs = selectedPairDao.getAllSelectedPairs().map { it.commonName }
        _selectedPairs.value = if (savedPairs.isEmpty()) defaultPairs else savedPairs
    }

    suspend fun savePairs(pairs: List<String>) {
        selectedPairDao.clearSelectedPairs()
        selectedPairDao.insertSelectedPairs(pairs.map { SelectedPairEntity(it) })
        _selectedPairs.value = pairs
    }

    suspend fun getCoinExTickers(): List<String> {
        return getTickersForExchange { it.coinexTicket }
    }

    suspend fun getGateIoTickers(): List<String> {
        return getTickersForExchange { it.gateioTicket }
    }

    suspend fun getHuobiTickers(): List<String> {
        return getTickersForExchange { it.huobiTicket }
    }

    private suspend fun getTickersForExchange(getTicker: (CurrencyPairEntity) -> String?): List<String> {
        val selectedPairs = _selectedPairs.value.toSet()
        return currencyPairDao.getAllPairs()
            .filter { it.commonName in selectedPairs }
            .mapNotNull(getTicker) // Исключаем null-значения
    }
}