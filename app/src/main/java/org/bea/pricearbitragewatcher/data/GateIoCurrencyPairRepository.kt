package org.bea.pricearbitragewatcher.data

import kotlinx.coroutines.flow.Flow
import org.bea.pricearbitragewatcher.net.GateIoWsV4Api
import java.util.concurrent.TimeUnit
import javax.inject.Inject

open class GateIoCurrencyPairRepository @Inject constructor(
    private val api: GateIoWsV4Api,
    private val currencyPairDao: GateIoCurrencyPairDao
) {
    suspend fun fetchAndCacheCurrencyPairsIfNeeded() {
        val lastUpdateTime = currencyPairDao.getLastUpdateTime() ?: 0
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastUpdateTime < TimeUnit.DAYS.toMillis(1)) {
            return // Обновление не требуется
        }

        val currencyPairs = api.getCurrencyPairs()
        val currencyPairEntities = currencyPairs.map { currencyPair ->
            GateIoCurrencyPairEntity(
                id = currencyPair.id,
                base = currencyPair.base,
                baseName = currencyPair.base_name,
                quote = currencyPair.quote,
                quoteName = currencyPair.quote_name,
                fee = currencyPair.fee,
                minBaseAmount = currencyPair.min_base_amount,
                minQuoteAmount = currencyPair.min_quote_amount,
                maxQuoteAmount = currencyPair.max_quote_amount,
                amountPrecision = currencyPair.amount_precision,
                precision = currencyPair.precision,
                tradeStatus = currencyPair.trade_status,
                sellStart = currencyPair.sell_start,
                buyStart = currencyPair.buy_start,
                type = currencyPair.type,
                lastUpdated = currentTime
            )
        }
        currencyPairDao.clearCurrencyPairs()
        currencyPairDao.insertCurrencyPairs(currencyPairEntities)
    }

    fun getCurrencyPairs(): Flow<List<GateIoCurrencyPairEntity>> = currencyPairDao.getAllCurrencyPairs()
}
