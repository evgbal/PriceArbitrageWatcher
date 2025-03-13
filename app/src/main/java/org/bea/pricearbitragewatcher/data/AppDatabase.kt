package org.bea.pricearbitragewatcher.data

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "current_ticker",
    primaryKeys = ["exchange", "symbol"],
    indices = [
        Index(value = ["symbol"])
        , Index(value = ["timestamp"])
        , Index(value = ["bid"])
        , Index(value = ["ask"])
    ]
)
data class CurrentTickerEntity(
    @ColumnInfo(name = "symbol") val symbol: String, // Символ инструмента
    @ColumnInfo(name = "exchange") val exchange: String, // Биржа (например, Gate.io, CoinEx, Huobi)
    @ColumnInfo(name = "bid") val bid: Double, // Лучшая цена покупки (Bid)
    @ColumnInfo(name = "ask") val ask: Double, // Лучшая цена продажи (Ask)
    @ColumnInfo(name = "timestamp") val timestamp: Long // Время последнего обновления
)

@Entity(tableName = "ticker_history",
    primaryKeys = ["exchange", "symbol", "timestamp"],
    indices = [
        Index(value = ["symbol"])
        , Index(value = ["timestamp"])
        , Index(value = ["bid"])
        , Index(value = ["ask"])
    ]
)
data class TickerHistoryEntity(
    @ColumnInfo(name = "symbol") val symbol: String, // Символ инструмента
    @ColumnInfo(name = "exchange") val exchange: String, // Биржа (например, Gate.io, CoinEx, Huobi)
    @ColumnInfo(name = "bid") val bid: Double, // Лучшая цена покупки (Bid)
    @ColumnInfo(name = "ask") val ask: Double, // Лучшая цена продажи (Ask)
    @ColumnInfo(name = "timestamp") val timestamp: Long // Время котировки
)

@Dao
interface TickerDao {
    // Вставка списка объектов
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickers(tickers: List<CurrentTickerEntity>)

    // Вставка списка истории котировок
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTickerHistory(history: List<TickerHistoryEntity>)

    // Получение всех актуальных котировок для символа и биржи
    @Query("SELECT * FROM current_ticker WHERE symbol = :symbol")
    suspend fun getCurrentTickers(symbol: String): List<CurrentTickerEntity>

    // Получение всех актуальных котировок для символа и биржи
    @Query("SELECT * FROM current_ticker WHERE symbol in (:symbols)")
    suspend fun getCurrentTickers(symbols: List<String>): List<CurrentTickerEntity>

    // Получение истории котировок для символа и биржи, отсортированное по времени
    @Query("SELECT * FROM ticker_history WHERE symbol = :symbol ORDER BY timestamp DESC")
    suspend fun getTickerHistory(symbol: String): List<TickerHistoryEntity>

    // Получение всех актуальных котировок для списка символов
    @Query("SELECT * FROM current_ticker WHERE symbol IN (:symbols)")
    suspend fun getCurrentTickersForSymbols(symbols: List<String>): List<CurrentTickerEntity>

    // Получение истории котировок для списка символов, отсортированное по времени
    @Query("SELECT * FROM ticker_history WHERE symbol IN (:symbols) ORDER BY timestamp DESC")
    suspend fun getTickerHistoryForSymbols(symbols: List<String>): List<TickerHistoryEntity>
}

@Entity(tableName = "currency_pairs")
data class CurrencyPairEntity(
    @PrimaryKey @ColumnInfo(name = "common_name") val commonName: String, // Например, "BTC/USDT"
    @ColumnInfo(name = "base_asset") val baseAsset: String, // BTC
    @ColumnInfo(name = "quote_asset") val quoteAsset: String, // USDT
    @ColumnInfo(name = "huobi_ticket") val huobiTicket: String?, // Название тикета на бирже Huobi
    @ColumnInfo(name = "coinex_ticket") val coinexTicket: String?, // Название тикета на бирже CoinEx
    @ColumnInfo(name = "gateio_ticket") val gateioTicket: String? // Название тикета на бирже Gate.io
)

@Dao
interface CurrencyPairDao {
    @Query("SELECT * FROM currency_pairs")
    suspend fun getAllPairs(): List<CurrencyPairEntity>

    @Query("SELECT * FROM currency_pairs where common_name in (:commonNames)")
    suspend fun getPairs(commonNames: List<String>): List<CurrencyPairEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPairs(pairs: List<CurrencyPairEntity>)

    @Query("DELETE FROM currency_pairs")
    suspend fun clearAll()

    @Delete
    suspend fun deletePair(pair: CurrencyPairEntity) // Удаление одной записи
}


@Entity(tableName = "coinex_market_info")
data class CoinExMarketEntity(
    @PrimaryKey
    @ColumnInfo(name = "market")
    val market: String,

    @ColumnInfo(name = "base_ccy")
    val baseCcy: String,

    @ColumnInfo(name = "base_ccy_precision")
    val baseCcyPrecision: Int,

    @ColumnInfo(name = "is_amm_available")
    val isAmmAvailable: Boolean,

    @ColumnInfo(name = "is_api_trading_available")
    val isApiTradingAvailable: Boolean,

    @ColumnInfo(name = "is_margin_available")
    val isMarginAvailable: Boolean,

    @ColumnInfo(name = "is_pre_market_trading_available")
    val isPreMarketTradingAvailable: Boolean,

    @ColumnInfo(name = "maker_fee_rate")
    val makerFeeRate: String,

    @ColumnInfo(name = "min_amount")
    val minAmount: String,

    @ColumnInfo(name = "quote_ccy")
    val quoteCcy: String,

    @ColumnInfo(name = "quote_ccy_precision")
    val quoteCcyPrecision: Int,

    @ColumnInfo(name = "taker_fee_rate")
    val takerFeeRate: String,

    @ColumnInfo(name = "last_updated")
    val lastUpdated: Long // Время последнего обновления
)

@Dao
interface CoinExMarketDao {
    @Query("SELECT * FROM coinex_market_info")
    fun getAllMarkets(): Flow<List<CoinExMarketEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMarkets(markets: List<CoinExMarketEntity>)

    @Query("DELETE FROM coinex_market_info")
    suspend fun clearMarkets()

    @Query("SELECT MAX(last_updated) FROM coinex_market_info")
    suspend fun getLastUpdateTime(): Long?
}



@Entity(tableName = "gateio_currency_pairs")
data class GateIoCurrencyPairEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "base") val base: String,
    @ColumnInfo(name = "base_name") val baseName: String,
    @ColumnInfo(name = "quote") val quote: String,
    @ColumnInfo(name = "quote_name") val quoteName: String,
    @ColumnInfo(name = "fee") val fee: String,
    @ColumnInfo(name = "min_base_amount") val minBaseAmount: String,
    @ColumnInfo(name = "min_quote_amount") val minQuoteAmount: String,
    @ColumnInfo(name = "max_quote_amount") val maxQuoteAmount: String?,
    @ColumnInfo(name = "amount_precision") val amountPrecision: Int,
    @ColumnInfo(name = "precision") val precision: Int,
    @ColumnInfo(name = "trade_status") val tradeStatus: String,
    @ColumnInfo(name = "sell_start") val sellStart: Long,
    @ColumnInfo(name = "buy_start") val buyStart: Long,
    @ColumnInfo(name = "type") val type: String,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long // Время последнего обновления
)


@Dao
interface GateIoCurrencyPairDao {
    @Query("SELECT * FROM gateio_currency_pairs")
    fun getAllCurrencyPairs(): Flow<List<GateIoCurrencyPairEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencyPairs(currencyPairs: List<GateIoCurrencyPairEntity>)

    @Query("DELETE FROM gateio_currency_pairs")
    suspend fun clearCurrencyPairs()

    @Query("SELECT MAX(last_updated) FROM gateio_currency_pairs")
    suspend fun getLastUpdateTime(): Long?
}


@Entity(tableName = "huobi_currency_pairs")
data class HuobiCurrencyPairEntity(
    @PrimaryKey val symbol: String,
    @ColumnInfo(name = "base_currency") val baseCurrency: String,
    @ColumnInfo(name = "quote_currency") val quoteCurrency: String,
    @ColumnInfo(name = "price_precision") val pricePrecision: Int,
    @ColumnInfo(name = "amount_precision") val amountPrecision: Int,
    @ColumnInfo(name = "symbol_partition") val symbolPartition: String,
    @ColumnInfo(name = "state") val state: String,
    @ColumnInfo(name = "value_precision") val valuePrecision: Int,
    @ColumnInfo(name = "min_order_amt") val minOrderAmt: Double,
    @ColumnInfo(name = "max_order_amt") val maxOrderAmt: Double,
    @ColumnInfo(name = "min_order_value") val minOrderValue: Double,
    @ColumnInfo(name = "limit_order_min_order_amt") val limitOrderMinOrderAmt: Double,
    @ColumnInfo(name = "limit_order_max_order_amt") val limitOrderMaxOrderAmt: Double,
    @ColumnInfo(name = "limit_order_max_buy_amt") val limitOrderMaxBuyAmt: Double,
    @ColumnInfo(name = "limit_order_max_sell_amt") val limitOrderMaxSellAmt: Double,
    @ColumnInfo(name = "buy_limit_must_less_than") val buyLimitMustLessThan: Double,
    @ColumnInfo(name = "sell_limit_must_greater_than") val sellLimitMustGreaterThan: Double,
    @ColumnInfo(name = "sell_market_min_order_amt") val sellMarketMinOrderAmt: Double,
    @ColumnInfo(name = "sell_market_max_order_amt") val sellMarketMaxOrderAmt: Double,
    @ColumnInfo(name = "buy_market_max_order_value") val buyMarketMaxOrderValue: Double,
    @ColumnInfo(name = "market_sell_order_rate_must_less_than") val marketSellOrderRateMustLessThan: Double,
    @ColumnInfo(name = "market_buy_order_rate_must_less_than") val marketBuyOrderRateMustLessThan: Double,
    @ColumnInfo(name = "api_trading") val apiTrading: String,
    @ColumnInfo(name = "tags") val tags: String?,
    @ColumnInfo(name = "last_updated") val lastUpdated: Long // Время последнего обновления
)


@Dao
interface HuobiCurrencyPairDao {
    @Query("SELECT * FROM huobi_currency_pairs")
    fun getAllCurrencyPairs(): Flow<List<HuobiCurrencyPairEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencyPairs(currencyPairs: List<HuobiCurrencyPairEntity>)

    @Query("DELETE FROM huobi_currency_pairs")
    suspend fun clearCurrencyPairs()

    @Query("SELECT MAX(last_updated) FROM huobi_currency_pairs")
    suspend fun getLastUpdateTime(): Long?
}


@Entity(tableName = "selected_pairs")
data class SelectedPairEntity(
    @PrimaryKey @ColumnInfo(name = "common_name") val commonName: String
)

@Dao
interface SelectedPairDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(pair: SelectedPairEntity)

    @Delete
    suspend fun delete(pair: SelectedPairEntity)

    @Query("SELECT * FROM selected_pairs")
    suspend fun getAllSelectedPairs(): List<SelectedPairEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM selected_pairs WHERE common_name = :pairName)")
    suspend fun isPairSelected(pairName: String): Boolean

    @Query("DELETE FROM selected_pairs")
    suspend fun clearSelectedPairs()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSelectedPairs(pairs: List<SelectedPairEntity>)

}



@Entity(
    tableName = "arbitrage_routes",
    primaryKeys = ["base_symbol", "quote_symbol", "start_exchange", "end_exchange"]
)
data class ArbitrageRouteEntity(
    @ColumnInfo(name = "base_symbol")
    val baseSymbol: String,   // Базовая валюта (например, BTC)

    @ColumnInfo(name = "quote_symbol")
    val quoteSymbol: String,  // Котируемая валюта (например, USDT)

    @ColumnInfo(name = "start_exchange")
    val startExchange: String, // Биржа покупки

    @ColumnInfo(name = "end_exchange")
    val endExchange: String,  // Биржа продажи

    @ColumnInfo(name = "start_bid")
    val startBid: Double,     // Цена покупки

    @ColumnInfo(name = "end_ask")
    val endAsk: Double,       // Цена продажи

    @ColumnInfo(name = "profit_percentage")
    val profitPercentage: Double, // Процент прибыли

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis() // Время последнего обновления
)


@Entity(tableName = "arbitrage_routes_history")
data class ArbitrageRouteHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,

    @ColumnInfo(name = "base_symbol") val baseSymbol: String,
    @ColumnInfo(name = "quote_symbol") val quoteSymbol: String,
    @ColumnInfo(name = "start_exchange") val startExchange: String,
    @ColumnInfo(name = "end_exchange") val endExchange: String,
    @ColumnInfo(name = "start_bid") val startBid: Double,
    @ColumnInfo(name = "end_ask") val endAsk: Double,
    @ColumnInfo(name = "profit_percentage") val profitPercentage: Double,
    @ColumnInfo(name = "snapshot_time") val snapshotTime: Long = System.currentTimeMillis() // Время фиксации снепшота
)


@Dao
interface ArbitrageRouteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArbitrageRoutes(routes: List<ArbitrageRouteEntity>)

    @Query("SELECT * FROM arbitrage_routes ORDER BY profit_percentage DESC")
    fun getAllArbitrageRoutes(): List<ArbitrageRouteEntity>

    @Query("DELETE FROM arbitrage_routes WHERE timestamp < :timeThreshold")
    suspend fun deleteOldArbitrageRoutes(timeThreshold: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertArbitrageHistory(routes: List<ArbitrageRouteHistoryEntity>)

    @Query("SELECT * FROM arbitrage_routes_history WHERE snapshot_time >= :startTime ORDER BY snapshot_time DESC")
    fun getArbitrageHistory(startTime: Long): List<ArbitrageRouteHistoryEntity>

    @Query("DELETE FROM arbitrage_routes_history WHERE snapshot_time < :timeThreshold")
    suspend fun deleteOldArbitrageHistory(timeThreshold: Long)
}






@Database(
    entities = [
          CurrentTickerEntity::class
        , TickerHistoryEntity::class
        , CurrencyPairEntity::class
        , CoinExMarketEntity::class
        , GateIoCurrencyPairEntity::class
        , HuobiCurrencyPairEntity::class
        , SelectedPairEntity::class
        , ArbitrageRouteEntity::class
        , ArbitrageRouteHistoryEntity::class
    ]
    , version = 1
    , exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun tickerDao(): TickerDao
    abstract fun currencyPairDao(): CurrencyPairDao
    abstract fun coinExMarketDao(): CoinExMarketDao
    abstract fun gateIoCurrencyPairDao(): GateIoCurrencyPairDao
    abstract fun huobiCurrencyPairDao(): HuobiCurrencyPairDao
    abstract fun selectedPairDao(): SelectedPairDao
    abstract fun arbitrageRouteDao(): ArbitrageRouteDao


}