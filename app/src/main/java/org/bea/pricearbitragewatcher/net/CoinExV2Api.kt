package org.bea.pricearbitragewatcher.net


import retrofit2.http.GET
import retrofit2.http.Query

////// Пример структуры запроса, если нужно
//data class SubscriptionMessage(
//    val time: Long = System.currentTimeMillis() / 1000,
//    //val method: String,
//    //val params: Params
//    val channel: String,
//    val event: String = "subscribe",
//    val payload: List<String> = emptyList()
//)


data class CoinExResponse(
    val code: Int,
    val message: String,
    val data: List<Market>
)

data class Market(
    val base_ccy: String,
    val base_ccy_precision: Int,
    val is_amm_available: Boolean,
    val is_api_trading_available: Boolean,
    val is_margin_available: Boolean,
    val is_pre_market_trading_available: Boolean,
    val maker_fee_rate: String,
    val market: String,
    val min_amount: String,
    val quote_ccy: String,
    val quote_ccy_precision: Int,
    val taker_fee_rate: String
)

data class CoinExOrderBookResponse(
    val code: Int,
    val message: String,
    val data: OrderBookData
)

data class OrderBookData(
    val asks: List<List<String>>,
    val bids: List<List<String>>,
    val last: String,
    val time: Long
)

interface CoinExV2Api {
    @GET("v2/spot/market")
    suspend fun getMarketData(): CoinExResponse

    @GET("v1/market/depth")
    suspend fun getOrderBook(
        @Query("market") market: String,
        @Query("limit") limit: Int,
        @Query("merge") merge: Int
    ): CoinExOrderBookResponse
}

