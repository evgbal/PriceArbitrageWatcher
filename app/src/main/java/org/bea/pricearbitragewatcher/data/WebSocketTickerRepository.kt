package org.bea.pricearbitragewatcher.data

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bea.pricearbitragewatcher.data.Constants.Companion.COIN_EX
import org.bea.pricearbitragewatcher.data.Constants.Companion.GATE_IO
import org.bea.pricearbitragewatcher.data.Constants.Companion.HUOBI
import org.bea.pricearbitragewatcher.net.CoinExWebSocketClient
import org.bea.pricearbitragewatcher.net.GateIoWebSocketClient
import org.bea.pricearbitragewatcher.net.HuobiWebSocketClient
import javax.inject.Inject
import javax.inject.Singleton


//private const val COIN_EX = "CoinEx"
//private const val GATE_IO = "Gate.io"
//private const val HUOBI = "Huobi"

@Singleton
class WebSocketTickerRepository @Inject constructor(
    private val tickerDao: TickerDao,
    private val marketPairsRepository: MarketPairsRepository,
    private val arbitrageRouteDao: ArbitrageRouteDao,
    private val currencyPairDao: CurrencyPairDao

) {

    private val _arbitrageRoutes = MutableStateFlow<List<ArbitrageRoute>>(emptyList())
    val arbitrageRoutes: StateFlow<List<ArbitrageRoute>> = _arbitrageRoutes.asStateFlow()

    private var job: Job? = null
    fun collectWebSocketData() {
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                combine(
                    CoinExWebSocketClient.messageFlow.map { COIN_EX to it }
                        .catch { e -> Log.e("WebSocket", "Ошибка CoinEx WS", e) },
                    GateIoWebSocketClient.messageFlow.map { GATE_IO to it }
                        .catch { e -> Log.e("WebSocket", "Ошибка Gate.io WS", e) },
                    HuobiWebSocketClient.messageFlow.map { HUOBI to it }
                        .catch { e -> Log.e("WebSocket", "Ошибка Huobi WS", e) }
                ) { messages ->
                    messages.mapNotNull { (exchange, json) ->
                        try {
                            parseJsonToTickerEntity(json, exchange)
                        } catch (e: Exception) {
                            Log.e("WebSocket", "Ошибка парсинга JSON ($exchange): $json", e)
                            null
                        }
                    }.flatten()
                }.catch { e ->
                    Log.e("WebSocket", "Ошибка в combine WebSocket потоков", e)
                }.collect { tickers ->
                    val filteredTickers = tickers.filter { it.symbol.isNotBlank() }
                    if (filteredTickers.isNotEmpty()) {
                        try {
                            tickerDao.insertTickers(filteredTickers)
                            tickerDao.insertTickerHistory(filteredTickers.map {
                                TickerHistoryEntity(
                                    symbol = it.symbol,
                                    exchange = it.exchange,
                                    bid = it.bid,
                                    ask = it.ask,
                                    timestamp = it.timestamp
                                )
                            })
                            val symbols = filteredTickers.map { it.symbol }
                            if (symbols.isNotEmpty()) {
                                val currentTickers = tickerDao.getCurrentTickers(symbols)
                                val arbitrageRoutes = findArbitrageRoutes(currentTickers)
                                if (arbitrageRoutes.isNotEmpty()) {
                                    arbitrageRouteDao.insertArbitrageRoutes(arbitrageRoutes.map {
                                        ArbitrageRouteEntity(
                                            baseSymbol = it.baseSymbol,
                                            quoteSymbol = it.quoteSymbol,
                                            startExchange = it.startExchange,
                                            endExchange = it.endExchange,
                                            startBid = it.startBid,
                                            endAsk = it.endAsk,
                                            profitPercentage = it.profitPercentage
                                        )
                                    })

                                    arbitrageRouteDao.insertArbitrageHistory(arbitrageRoutes.map {
                                        ArbitrageRouteHistoryEntity(
                                            baseSymbol = it.baseSymbol,
                                            quoteSymbol = it.quoteSymbol,
                                            startExchange = it.startExchange,
                                            endExchange = it.endExchange,
                                            startBid = it.startBid,
                                            endAsk = it.endAsk,
                                            profitPercentage = it.profitPercentage,
                                            snapshotTime = System.currentTimeMillis() // Сохраняем текущее время как снимок
                                        )
                                    })
                                }
                            }


                            val timeThresholdCurrent =
                                System.currentTimeMillis() - (30 * 1000) // 1 день назад
                            arbitrageRouteDao.deleteOldArbitrageRoutes(timeThresholdCurrent)
                            val timeThresholdHistory =
                                System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 1 день назад
                            arbitrageRouteDao.deleteOldArbitrageHistory(timeThresholdHistory)

                            _arbitrageRoutes.value = arbitrageRouteDao.getAllArbitrageRoutes().map {
                                ArbitrageRoute(
                                    baseSymbol = it.baseSymbol,
                                    quoteSymbol = it.quoteSymbol,
                                    startExchange = it.startExchange,
                                    endExchange = it.endExchange,
                                    startBid = it.startBid,
                                    endAsk = it.endAsk,
                                    profitPercentage = it.profitPercentage

                                )
                            }

                        } catch (e: Exception) {
                            Log.e("WebSocket", "Ошибка при сохранении данных в БД", e)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("WebSocket", "Критическая ошибка в collectWebSocketData", e)
            }
        }
    }

    private val gson = Gson()

    private suspend fun parseJsonToTickerEntity(
        json: String,
        exchange: String
    ): List<CurrentTickerEntity> {
        return try {
            when (exchange) {
                COIN_EX -> {
                    val jsonObject = gson.fromJson(json, JsonObject::class.java)
                    when (jsonObject["method"]?.asString) {
                        "state.update" -> {
                            val paramsArray = jsonObject["params"]?.asJsonArray
                            if (paramsArray != null && paramsArray.size() > 0) {
                                val tickerData = paramsArray[0].asJsonObject
                                tickerData.entrySet().map { entry ->
                                    val data = entry.value.asJsonObject
                                    val lastPrice = data["last"]?.asDouble ?: 0.0
                                    val symbol = withContext(Dispatchers.IO) {
                                        marketPairsRepository.fromExchangeTicket(entry.key, COIN_EX)
                                    }

                                    CurrentTickerEntity(
                                        symbol = symbol ?: "", //entry.key,
                                        exchange = exchange,
                                        bid = lastPrice, // Для старых сообщений берём last
                                        ask = lastPrice,
                                        timestamp = System.currentTimeMillis()
                                    )
                                }
                            } else emptyList()
                        }

                        "bbo.update" -> {
                            val params = jsonObject["params"]?.asJsonObject
                            if (params != null) {
                                val symbol = withContext(Dispatchers.IO) {
                                    marketPairsRepository.fromExchangeTicket(
                                        params["market"]?.asString ?: "", COIN_EX
                                    )
                                }

                                listOf(
                                    CurrentTickerEntity(
                                        symbol = symbol ?: "",//params["market"]?.asString ?: "",
                                        exchange = exchange,
                                        bid = params["bid_price"]?.asDouble ?: 0.0,
                                        ask = params["ask_price"]?.asDouble ?: 0.0,
                                        timestamp = params["time"]?.asLong
                                            ?: System.currentTimeMillis()
                                    )
                                )
                            } else emptyList()
                        }

                        else -> emptyList()
                    }
                }

                GATE_IO -> {
                    val jsonObject = gson.fromJson(json, JsonObject::class.java)
                    val result = jsonObject["result"]?.asJsonObject
                    if (result != null) {
                        val symbol = withContext(Dispatchers.IO) {
                            marketPairsRepository.fromExchangeTicket(
                                result["currency_pair"]?.asString ?: "", GATE_IO
                            )
                        }
                        //val symbol =  result["currency_pair"]?.asString ?: "" //result["currency_pair"]?.asString?.replace("_", "")?.uppercase()
                        if (!symbol.isNullOrBlank()) { // Проверяем, что символ не пустой
                            listOf(
                                CurrentTickerEntity(
                                    symbol = symbol,
                                    exchange = exchange,
                                    bid = result["highest_bid"]?.asDouble ?: 0.0,
                                    ask = result["lowest_ask"]?.asDouble ?: 0.0,
                                    timestamp = jsonObject["time_ms"]?.asLong
                                        ?: System.currentTimeMillis()
                                )
                            )
                        } else emptyList() // Если символ пустой, не добавляем в список
                    } else emptyList()
                }

                HUOBI -> {
                    val jsonObject = gson.fromJson(json, JsonObject::class.java)
                    val tick = jsonObject["tick"]?.asJsonObject
                    if (tick != null) {

                        val symbol = withContext(Dispatchers.IO) {
                            marketPairsRepository.fromExchangeTicket(
                                jsonObject["ch"]?.asString?.split(
                                    "."
                                )?.get(1) ?: "", HUOBI
                            )
                        }
                        listOf(
                            CurrentTickerEntity(
                                symbol = symbol
                                    ?: "",//jsonObject["ch"]?.asString?.split(".")?.get(1)?.uppercase() ?: "",
                                exchange = exchange,
                                bid = tick["bid"]?.asDouble ?: 0.0,
                                ask = tick["ask"]?.asDouble ?: 0.0,
                                timestamp = jsonObject["ts"]?.asLong ?: System.currentTimeMillis()
                            )
                        )
                    } else emptyList()
                }

                else -> emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


    private suspend fun findArbitrageRoutes(tickers: List<CurrentTickerEntity>): List<ArbitrageRoute> {
        val routes = mutableListOf<ArbitrageRoute>()
        val groupedTickers = tickers.groupBy { it.symbol }

        val currencyPairMap: Map<String, CurrencyPairEntity> =
            currencyPairDao.getPairs(tickers.map { it.symbol }).associateBy { it.commonName }


        for ((symbol, tickersList) in groupedTickers) {
            for (buy in tickersList) {
                for (sell in tickersList) {
                    if (buy.exchange != sell.exchange && buy.ask < sell.bid) {
                        val profitPercentage = ((sell.bid - buy.ask) / buy.ask) * 100.0
                        //if (profitPercentage > 0.5) { // Фильтруем малоприбыльные сделки
                        if (profitPercentage > 0.01) {
                            routes.add(
                                ArbitrageRoute(
                                    baseSymbol = currencyPairMap[symbol]?.baseAsset
                                        ?: "", //symbol.split("_")[0],
                                    quoteSymbol = currencyPairMap[symbol]?.quoteAsset
                                        ?: "", //symbol.split("_")[1],
                                    startExchange = buy.exchange,
                                    endExchange = sell.exchange,
                                    startBid = buy.ask,
                                    endAsk = sell.bid,
                                    profitPercentage = profitPercentage
                                )
                            )
                        }
                    }
                }
            }
        }
        return routes
    }


}


data class ArbitrageRoute(
    val baseSymbol: String,
    val quoteSymbol: String,
    val startExchange: String,
    val endExchange: String,
    val startBid: Double,
    val endAsk: Double,
    val profitPercentage: Double) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ArbitrageRoute) return false

        return baseSymbol == other.baseSymbol &&
                quoteSymbol == other.quoteSymbol &&
                startExchange == other.startExchange &&
                endExchange == other.endExchange &&
                startBid == other.startBid &&
                endAsk == other.endAsk &&
                profitPercentage == other.profitPercentage
    }

    override fun hashCode(): Int {
        var result = baseSymbol.hashCode()
        result = 31 * result + quoteSymbol.hashCode()
        result = 31 * result + startExchange.hashCode()
        result = 31 * result + endExchange.hashCode()
        result = 31 * result + startBid.hashCode()
        result = 31 * result + endAsk.hashCode()
        result = 31 * result + profitPercentage.hashCode()
        return result
    }
}
