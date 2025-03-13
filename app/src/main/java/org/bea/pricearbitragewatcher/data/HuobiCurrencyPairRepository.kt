package org.bea.pricearbitragewatcher.data

import kotlinx.coroutines.flow.Flow
import org.bea.pricearbitragewatcher.net.HuobiProV1Api
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class HuobiCurrencyPairRepository @Inject constructor(
    private val api: HuobiProV1Api,
    private val currencyPairDao: HuobiCurrencyPairDao
) {
    suspend fun fetchAndCacheCurrencyPairsIfNeeded() {
        val lastUpdateTime = currencyPairDao.getLastUpdateTime() ?: 0
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastUpdateTime < TimeUnit.DAYS.toMillis(1)) {
            return // Обновление не требуется
        }

        val response = api.getCurrencyPairs()
        val currencyPairEntities = response.data.map { currencyPair ->
            HuobiCurrencyPairEntity(
                symbol = currencyPair.symbol,
                baseCurrency = currencyPair.baseCurrency,
                quoteCurrency = currencyPair.quoteCurrency,
                pricePrecision = currencyPair.pricePrecision,
                amountPrecision = currencyPair.amountPrecision,
                symbolPartition = currencyPair.symbolPartition,
                state = currencyPair.state,
                valuePrecision = currencyPair.valuePrecision,
                minOrderAmt = currencyPair.minOrderAmt,
                maxOrderAmt = currencyPair.maxOrderAmt,
                minOrderValue = currencyPair.minOrderValue,
                limitOrderMinOrderAmt = currencyPair.limitOrderMinOrderAmt,
                limitOrderMaxOrderAmt = currencyPair.limitOrderMaxOrderAmt,
                limitOrderMaxBuyAmt = currencyPair.limitOrderMaxBuyAmt,
                limitOrderMaxSellAmt = currencyPair.limitOrderMaxSellAmt,
                buyLimitMustLessThan = currencyPair.buyLimitMustLessThan,
                sellLimitMustGreaterThan = currencyPair.sellLimitMustGreaterThan,
                sellMarketMinOrderAmt = currencyPair.sellMarketMinOrderAmt,
                sellMarketMaxOrderAmt = currencyPair.sellMarketMaxOrderAmt,
                buyMarketMaxOrderValue = currencyPair.buyMarketMaxOrderValue,
                marketSellOrderRateMustLessThan = currencyPair.marketSellOrderRateMustLessThan,
                marketBuyOrderRateMustLessThan = currencyPair.marketBuyOrderRateMustLessThan,
                apiTrading = currencyPair.apiTrading,
                tags = currencyPair.tags,
                lastUpdated = currentTime
            )
        }

        currencyPairDao.clearCurrencyPairs()
        currencyPairDao.insertCurrencyPairs(currencyPairEntities)
    }

    fun getCurrencyPairs(): Flow<List<HuobiCurrencyPairEntity>> = currencyPairDao.getAllCurrencyPairs()
}
