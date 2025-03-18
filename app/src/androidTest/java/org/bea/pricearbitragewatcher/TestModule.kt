package org.bea.pricearbitragewatcher

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.flow.MutableStateFlow
import org.bea.pricearbitragewatcher.ui.CurrencyPairSelectionViewModel

import kotlinx.coroutines.CoroutineScope
import org.bea.pricearbitragewatcher.data.ArbitrageRouteDao
import org.bea.pricearbitragewatcher.data.CoinExMarketRepository
import org.bea.pricearbitragewatcher.data.CurrencyPairDao
import org.bea.pricearbitragewatcher.data.CurrencyPairEntity
import org.bea.pricearbitragewatcher.data.DatabaseModule
import org.bea.pricearbitragewatcher.data.GateIoCurrencyPairRepository
import org.bea.pricearbitragewatcher.data.HuobiCurrencyPairRepository
import org.bea.pricearbitragewatcher.data.MarketPairsRepository
import org.bea.pricearbitragewatcher.data.SelectedPairDao
import org.bea.pricearbitragewatcher.data.SelectedPairEntity
import org.bea.pricearbitragewatcher.data.SelectedPairRepository
import org.bea.pricearbitragewatcher.data.TickerDao
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [DatabaseModule::class]
)
object TestModule {

    @Provides
    @Singleton
    fun provideCoinExMarketRepository(): CoinExMarketRepository {
        return mock<CoinExMarketRepository>()
    }

    @Provides
    @Singleton
    fun provideGateIoCurrencyPairRepository(): GateIoCurrencyPairRepository {
        return mock<GateIoCurrencyPairRepository>()
    }

    @Provides
    @Singleton
    fun provideHuobiCurrencyPairRepository(): HuobiCurrencyPairRepository {
        return mock<HuobiCurrencyPairRepository>()
    }

    @Provides
    @Singleton
    fun provideCurrencyPairDao(): CurrencyPairDao {
        return mock<CurrencyPairDao>()
    }

    @Provides
    @Singleton
    fun provideMarketPairsRepository(
        coinExMarketRepository: CoinExMarketRepository,
        gateIoCurrencyPairRepository: GateIoCurrencyPairRepository,
        huobiCurrencyPairRepository: HuobiCurrencyPairRepository,
        currencyPairDao: CurrencyPairDao,
        @ApplicationScope scope: CoroutineScope
    ): MarketPairsRepository {
        return MarketPairsRepository(
            coinExMarketRepository,
            gateIoCurrencyPairRepository,
            huobiCurrencyPairRepository,
            currencyPairDao,
            scope
        )
    }

    @Provides
    @Singleton
    fun provideSelectedPairDao(): SelectedPairDao {
        return mock<SelectedPairDao>()
    }

    @Provides
    @Singleton
    fun provideTickerDao(): TickerDao {
        return mock<TickerDao>()
    }

    @Provides
    @Singleton
    fun provideArbitrageRouteDao(): ArbitrageRouteDao {
        return mock<ArbitrageRouteDao>()
    }

    @Provides
    @Singleton
    fun provideSelectedPairRepository(
        selectedPairDao: SelectedPairDao,
        currencyPairDao: CurrencyPairDao
    ): SelectedPairRepository {
        return mock<SelectedPairRepository>()
    }

    @Provides
    @Singleton
    fun provideCurrencyPairSelectionViewModel(
        marketPairsRepository: MarketPairsRepository,
        selectedPairRepository: SelectedPairRepository
    ): CurrencyPairSelectionViewModel {
        return CurrencyPairSelectionViewModel(marketPairsRepository, selectedPairRepository)
    }
}