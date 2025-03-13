package org.bea.pricearbitragewatcher.net


import retrofit2.http.GET
import retrofit2.http.Query


//data class Params(val channel: String)

//data class SymbolResponse(
//    val id: String,
//    val name: String,
//    val base: String,
//    val quote: String
//)
/*
# coding: utf-8
import requests

host = "https://api.gateio.ws"
prefix = "/api/v4"
headers = {'Accept': 'application/json', 'Content-Type': 'application/json'}

url = '/spot/currency_pairs'
query_param = ''
r = requests.request('GET', host + prefix + url, headers=headers)
print(r.json())
*/

data class GateIoCurrencyPair(
    val id: String,
    val base: String,
    val base_name: String,
    val quote: String,
    val quote_name: String,
    val fee: String,
    val min_base_amount: String,
    val min_quote_amount: String,
    val max_quote_amount: String,
    val amount_precision: Int,
    val precision: Int,
    val trade_status: String,
    val sell_start: Long,
    val buy_start: Long,
    val type: String
)

data class GateIoOrderBookResponse(
    val current: Long,
    val update: Long,
    val asks: List<List<String>>, // Цены и количество для продаж
    val bids: List<List<String>>  // Цены и количество для покупок
)

interface GateIoWsV4Api {
//
//    @POST("v4/spot/subscribe")
//    fun subscribeToTickers(@Body message: SubscriptionMessage): Call<ResponseBody>

//
//    @GET("api2/1/symbols")
//    suspend fun getSpotSymbols(): Response<List<SymbolResponse>>

    @GET("api/v4/spot/currency_pairs")
    suspend fun getCurrencyPairs(): List<GateIoCurrencyPair>

    @GET("api/v4/spot/order_book")
    suspend fun getOrderBook(
        @Query("currency_pair") currencyPair: String,
        @Query("limit") limit: Int
    ): GateIoOrderBookResponse
}

