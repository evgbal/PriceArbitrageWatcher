package org.bea.pricearbitragewatcher.net


import com.google.gson.annotations.SerializedName
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


data class HuobiResponse(
    val status: String,
    val data: List<HuobiCurrencyPair>
)

data class HuobiCurrencyPair(
    @SerializedName("base-currency") val baseCurrency: String,
    @SerializedName("quote-currency") val quoteCurrency: String,
    @SerializedName("price-precision") val pricePrecision: Int,
    @SerializedName("amount-precision") val amountPrecision: Int,
    @SerializedName("symbol-partition") val symbolPartition: String,
    val symbol: String,
    val state: String,
    @SerializedName("value-precision") val valuePrecision: Int,
    @SerializedName("min-order-amt") val minOrderAmt: Double,
    @SerializedName("max-order-amt") val maxOrderAmt: Double,
    @SerializedName("min-order-value") val minOrderValue: Double,
    @SerializedName("limit-order-min-order-amt") val limitOrderMinOrderAmt: Double,
    @SerializedName("limit-order-max-order-amt") val limitOrderMaxOrderAmt: Double,
    @SerializedName("limit-order-max-buy-amt") val limitOrderMaxBuyAmt: Double,
    @SerializedName("limit-order-max-sell-amt") val limitOrderMaxSellAmt: Double,
    @SerializedName("buy-limit-must-less-than") val buyLimitMustLessThan: Double,
    @SerializedName("sell-limit-must-greater-than") val sellLimitMustGreaterThan: Double,
    @SerializedName("sell-market-min-order-amt") val sellMarketMinOrderAmt: Double,
    @SerializedName("sell-market-max-order-amt") val sellMarketMaxOrderAmt: Double,
    @SerializedName("buy-market-max-order-value") val buyMarketMaxOrderValue: Double,
    @SerializedName("market-sell-order-rate-must-less-than") val marketSellOrderRateMustLessThan: Double,
    @SerializedName("market-buy-order-rate-must-less-than") val marketBuyOrderRateMustLessThan: Double,
    @SerializedName("api-trading") val apiTrading: String,
    val tags: String
)


// Определяем структуру ответа
data class HuobiOrderBookResponse(
    val ch: String,
    val status: String,
    val ts: Long,
    val tick: HuobiOrderBookTick
)

data class HuobiOrderBookTick(
    val bids: List<List<Double>>, // [[цена, объем], ...]
    val asks: List<List<Double>>  // [[цена, объем], ...]
)


interface HuobiProV1Api {
    @GET("v1/common/symbols")
    suspend fun getCurrencyPairs(): HuobiResponse

    @GET("market/depth")
    suspend fun getOrderBook(
        @Query("symbol") symbol: String,
        @Query("type") type: String = "step0"
    ): HuobiOrderBookResponse
}

