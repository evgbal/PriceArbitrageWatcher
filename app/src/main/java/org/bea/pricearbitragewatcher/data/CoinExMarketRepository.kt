package org.bea.pricearbitragewatcher.data

import kotlinx.coroutines.flow.Flow
import org.bea.pricearbitragewatcher.net.CoinExV2Api
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CoinExMarketRepository @Inject constructor(
    private val api: CoinExV2Api,
    private val marketDao: CoinExMarketDao
) {
    suspend fun fetchAndCacheMarketsIfNeeded() {
        val lastUpdateTime = marketDao.getLastUpdateTime() ?: 0
        val currentTime = System.currentTimeMillis()

        if (currentTime - lastUpdateTime < TimeUnit.DAYS.toMillis(1)) {
            return // Обновление не требуется
        }

        val response = api.getMarketData()
        if (response.code == 0) {
            val marketEntities = response.data.map { market ->
                CoinExMarketEntity(
                    market = market.market,
                    baseCcy = market.base_ccy,
                    baseCcyPrecision = market.base_ccy_precision,
                    isAmmAvailable = market.is_amm_available,
                    isApiTradingAvailable = market.is_api_trading_available,
                    isMarginAvailable = market.is_margin_available,
                    isPreMarketTradingAvailable = market.is_pre_market_trading_available,
                    makerFeeRate = market.maker_fee_rate,
                    minAmount = market.min_amount,
                    quoteCcy = market.quote_ccy,
                    quoteCcyPrecision = market.quote_ccy_precision,
                    takerFeeRate = market.taker_fee_rate,
                    lastUpdated = currentTime
                )
            }
            marketDao.clearMarkets()
            marketDao.insertMarkets(marketEntities)
        }
    }

    fun getMarkets(): Flow<List<CoinExMarketEntity>> = marketDao.getAllMarkets()
}