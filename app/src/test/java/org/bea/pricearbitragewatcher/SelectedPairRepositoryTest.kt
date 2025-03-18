package org.bea.pricearbitragewatcher

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.bea.pricearbitragewatcher.data.SelectedPairRepository
import org.bea.pricearbitragewatcher.data.SelectedPairDao
import org.bea.pricearbitragewatcher.data.CurrencyPairDao
import org.bea.pricearbitragewatcher.data.SelectedPairEntity
import org.bea.pricearbitragewatcher.data.CurrencyPairEntity
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class SelectedPairRepositoryTest {

    @Mock
    private lateinit var selectedPairDao: SelectedPairDao

    @Mock
    private lateinit var currencyPairDao: CurrencyPairDao

    private lateinit var repository: SelectedPairRepository

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        repository = SelectedPairRepository(selectedPairDao, currencyPairDao)
    }

    @Test
    fun `loadPairs loads saved pairs when available`() = runTest {
        val savedPairs = listOf(SelectedPairEntity("BTC/USDT"), SelectedPairEntity("ETH/USDT"))
        `when`(selectedPairDao.getAllSelectedPairs()).thenReturn(savedPairs)

        repository.loadPairs()

        val result = repository.selectedPairs.first()
        assertEquals(listOf("BTC/USDT", "ETH/USDT"), result)
    }

    @Test
    fun `loadPairs loads default pairs when no saved pairs`() = runTest {
        `when`(selectedPairDao.getAllSelectedPairs()).thenReturn(emptyList())

        repository.loadPairs()

        val result = repository.selectedPairs.first()
        assertEquals(listOf("BTC/USDT", "LTC/USDT", "ETH/USDT", "ETHW/USDT"), result)
    }

    @Test
    fun `savePairs updates selected pairs`() = runTest {
        val newPairs = listOf("DOGE/USDT", "XRP/USDT")

        repository.savePairs(newPairs)

        verify(selectedPairDao).clearSelectedPairs()
        verify(selectedPairDao).insertSelectedPairs(newPairs.map { SelectedPairEntity(it) })

        val result = repository.selectedPairs.first()
        assertEquals(newPairs, result)
    }

    @Test
    fun `getCoinExTickers returns mapped tickers`() = runTest {
        val currencyPairs = listOf(
            CurrencyPairEntity("BTC/USDT", "BTC", "USDT", "BTCUSDT_HB", "BTCUSDT_CE", "BTCUSDT_GI"),
            CurrencyPairEntity("ETH/USDT", "ETH", "USDT", "ETHUSDT_HB", "ETHUSDT_CE", "ETHUSDT_GI")
        )
        `when`(currencyPairDao.getAllPairs()).thenReturn(currencyPairs)

        repository.savePairs(listOf("BTC/USDT", "ETH/USDT"))
        val tickers = repository.getCoinExTickers()

        assertEquals(listOf("BTCUSDT_CE", "ETHUSDT_CE"), tickers)
    }
}
