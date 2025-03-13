package org.bea.pricearbitragewatcher.data

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bea.pricearbitragewatcher.ApplicationScope
import org.bea.pricearbitragewatcher.data.Constants.Companion.COIN_EX
import org.bea.pricearbitragewatcher.data.Constants.Companion.GATE_IO
import org.bea.pricearbitragewatcher.data.Constants.Companion.HUOBI
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MarketPairsRepository @Inject constructor(
    private val coinExMarketRepository: CoinExMarketRepository,
    private val gateIoCurrencyPairRepository: GateIoCurrencyPairRepository,
    private val huobiCurrencyPairRepository: HuobiCurrencyPairRepository,
    private val currencyPairDao: CurrencyPairDao,
    //private val selectedPairRepository: SelectedPairRepository,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    private val coinExTicketMap = MutableStateFlow(emptyMap<String, String>())
    private val gateIoTicketMap = MutableStateFlow(emptyMap<String, String>())
    private val huobiTicketMap = MutableStateFlow(emptyMap<String, String>())
    private val commonToCoinExMap = MutableStateFlow(emptyMap<String, String>())
    private val commonToGateIoMap = MutableStateFlow(emptyMap<String, String>())
    private val commonToHuobiMap = MutableStateFlow(emptyMap<String, String>())


    init {
        applicationScope.launch {
            coinExMarketRepository.getMarkets().collect { markets ->
                coinExTicketMap.value = markets.associate { it.market to toCommonName(it.baseCcy, it.quoteCcy) }
                commonToCoinExMap.value = markets.associate { toCommonName(it.baseCcy, it.quoteCcy) to it.market }
            }
        }
        applicationScope.launch {
            gateIoCurrencyPairRepository.getCurrencyPairs().collect { pairs ->
                gateIoTicketMap.value = pairs.associate { it.id to toCommonName(it.base, it.quote) }
                commonToGateIoMap.value = pairs.associate { toCommonName(it.base, it.quote) to it.id }
            }
        }
        applicationScope.launch {
            huobiCurrencyPairRepository.getCurrencyPairs().collect { pairs ->
                huobiTicketMap.value = pairs.associate { it.symbol to toCommonName(it.baseCurrency, it.quoteCurrency) }
                commonToHuobiMap.value = pairs.associate { toCommonName(it.baseCurrency, it.quoteCurrency) to it.symbol }
            }
        }
    }

    val coinExPairs: Flow<List<CoinExMarketEntity>> =
        coinExMarketRepository.getMarkets().shareIn(applicationScope, SharingStarted.Lazily, replay = 1)

    val gateIoPairs: Flow<List<GateIoCurrencyPairEntity>> =
        gateIoCurrencyPairRepository.getCurrencyPairs().shareIn(applicationScope, SharingStarted.Lazily, replay = 1)

    val huobiPairs: Flow<List<HuobiCurrencyPairEntity>> =
        huobiCurrencyPairRepository.getCurrencyPairs().shareIn(applicationScope, SharingStarted.Lazily, replay = 1)

    suspend fun checkAndUpdateMarkets() = withContext(Dispatchers.IO) {
        coinExMarketRepository.fetchAndCacheMarketsIfNeeded()
        gateIoCurrencyPairRepository.fetchAndCacheCurrencyPairsIfNeeded()
        huobiCurrencyPairRepository.fetchAndCacheCurrencyPairsIfNeeded()
        updateMarketPairsInCache()
        //selectedPairRepository.loadPairs()
    }

    fun getActiveMarketPairsFlow(): Flow<List<String>> = flow {
        emit(getActiveMarketPairs().map { it.commonName }) // Преобразуем в список строк
    }

    suspend fun getActiveMarketPairs(): List<CurrencyPairEntity> {
        // Собираем данные из Flow с помощью collect()
        val coinExActivePairs = coinExMarketRepository.getMarkets().first()
            .filter { it.isApiTradingAvailable } // Фильтруем по доступности для торговли
            .map {
                CurrencyPairEntity(//"${it.baseCcy}/${it.quoteCcy}".trim().uppercase(),
                    toCommonName(it.baseCcy, it.quoteCcy),
                    it.baseCcy,
                    it.quoteCcy,
                    huobiTicket = null, // Пока пусто
                    coinexTicket = it.market, // Тикет для CoinEx
                    gateioTicket = null // Пока пусто
                )
            }

        val gateIoActivePairs = gateIoCurrencyPairRepository.getCurrencyPairs().first()
            .filter { it.tradeStatus == "tradable" } // Фильтруем по статусу торговли
            .map {
                CurrencyPairEntity(//"${it.base}/${it.quote}".trim().uppercase(),
                    toCommonName(it.base, it.quote),
                    it.base,
                    it.quote,
                    huobiTicket = null,
                    coinexTicket = null,
                    gateioTicket = it.id
                )
            }

        val huobiActivePairs = huobiCurrencyPairRepository.getCurrencyPairs().first()
            .filter { it.state == "online" && it.apiTrading == "enabled" } // Фильтруем по доступности для торговли
            .map { CurrencyPairEntity(
                //"${it.baseCurrency}/${it.quoteCurrency}".trim().uppercase(),
                toCommonName(it.baseCurrency, it.quoteCurrency),
                it.baseCurrency,
                it.quoteCurrency,
                huobiTicket = it.symbol,
                coinexTicket = null,
                gateioTicket = null
            )
         }

        // Находим пересечение всех активных пар
        val allPairs = coinExActivePairs + gateIoActivePairs + huobiActivePairs
        val commonPairs = allPairs
            .groupingBy { it.commonName }
            .eachCount()
            .filter { it.value == 3 } // Выбираем пары, которые встречаются на всех трех биржах

        // Возвращаем общие активные пары
        return commonPairs.keys.map { commonName ->
            // Фильтруем все пары с данным commonName и агрегация данных о тикетах
            val pair = allPairs.filter { it.commonName == commonName }

            // Извлекаем тикеты для каждой биржи
            val huobiTicket = pair.find { it.huobiTicket != null }?.huobiTicket
            val coinexTicket = pair.find { it.coinexTicket != null }?.coinexTicket
            val gateioTicket = pair.find { it.gateioTicket != null }?.gateioTicket

            // Возвращаем результат с тикетами
            CurrencyPairEntity(
                commonName,
                pair.first().baseAsset, // Берём базовый актив из первой найденной записи
                pair.first().quoteAsset, // Берём котируемый актив из первой найденной записи
                huobiTicket = huobiTicket,
                coinexTicket = coinexTicket,
                gateioTicket = gateioTicket
            )
        }
    }


    // Метод для обновления списка общих пар в базе данных без удаления всех записей
    suspend fun updateMarketPairsInCache() {
        val activePairs = getActiveMarketPairs()

        // Получаем текущие пары из базы данных
        val existingPairs = currencyPairDao.getAllPairs()

        // Создаем список пар для вставки и обновления
        val pairsToUpdate = mutableListOf<CurrencyPairEntity>()
        val pairsToDelete = mutableListOf<CurrencyPairEntity>()

        // Проходим по актуальным парам и обновляем их
        activePairs.forEach { newPair ->
            val existingPair = existingPairs.find { it.commonName == newPair.commonName }
            if (existingPair != null) {
                // Если пара существует, проверим, нужно ли обновлять (например, если какие-то данные изменились)
                // В вашем случае, если только commonName совпадает, то можно просто обновить
                // Если нужно проверить более детально, добавьте дополнительные условия сравнения
                if (existingPair != newPair) {
                    pairsToUpdate.add(newPair)
                }
            } else {
                // Если пара новая, добавляем её в список
                pairsToUpdate.add(newPair)
            }
        }

        // Проходим по старым данным, чтобы удалить пары, которых больше нет
        existingPairs.forEach { existingPair ->
            val isStillActive = activePairs.any { it.commonName == existingPair.commonName }
            if (!isStillActive) {
                pairsToDelete.add(existingPair)
            }
        }

        // Обновляем и удаляем записи
        pairsToDelete.forEach { pair ->
            currencyPairDao.deletePair(pair) // Добавьте метод удаления в DAO
        }
        if (pairsToUpdate.isNotEmpty()) {
            currencyPairDao.insertPairs(pairsToUpdate)
        }
    }

    private fun toCommonName(base: String, quote: String): String {
        return "${base.uppercase()}/${quote.uppercase()}".trim()
    }

//    suspend fun fromExchangeTicket(ticket: String, exchange: String): String? {
//        return when (exchange) {
//            COIN_EX -> coinExPairs.first().find { it.market == ticket }?.let { toCommonName(it.baseCcy, it.quoteCcy) }
//            GATE_IO -> gateIoPairs.first().find { it.id == ticket }?.let { toCommonName(it.base, it.quote) }
//            HUOBI -> huobiPairs.first().find { it.symbol == ticket }?.let { toCommonName(it.baseCurrency, it.quoteCurrency) }
//            else -> null
//        }
//    }

    fun fromExchangeTicket(ticket: String, exchange: String): String? {
        return when (exchange) {
            COIN_EX -> coinExTicketMap.value[ticket]
            GATE_IO -> gateIoTicketMap.value[ticket]
            HUOBI -> huobiTicketMap.value[ticket]
            else -> null
        }
    }

    fun toExchangeTicket(commonName: String, exchange: String): String? {
        return when (exchange) {
            COIN_EX -> commonToCoinExMap.value[commonName]
            GATE_IO -> commonToGateIoMap.value[commonName]
            HUOBI -> commonToHuobiMap.value[commonName]
            else -> null
        }
    }

}
