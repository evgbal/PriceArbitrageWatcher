package org.bea.pricearbitragewatcher

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bea.pricearbitragewatcher.data.MarketPairsRepository
import org.bea.pricearbitragewatcher.data.SelectedPairRepository
import org.bea.pricearbitragewatcher.data.WebSocketTickerRepository
import org.bea.pricearbitragewatcher.usecase.SubscribeToCoinExUseCase
import org.bea.pricearbitragewatcher.usecase.SubscribeToGateIoUseCase
import org.bea.pricearbitragewatcher.usecase.SubscribeToHuobiUseCase
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {
    // Delete when https://github.com/google/dagger/issues/3601 is resolved.
    @Inject @ApplicationContext lateinit var context: Context

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @Inject
    lateinit var subscribeToCoinExUseCase: SubscribeToCoinExUseCase
    @Inject
    lateinit var subscribeToGateIoUseCase: SubscribeToGateIoUseCase
    @Inject
    lateinit var subscribeToHuobiUseCase: SubscribeToHuobiUseCase

    @Inject
    lateinit var webSocketTickerRepository: WebSocketTickerRepository

    @Inject
    lateinit var selectedPairRepository: SelectedPairRepository

    @Inject
    lateinit var marketPairsRepository: MarketPairsRepository

    override fun onCreate() {
        super.onCreate()
//        (applicationContext as? App)?.let {
//            DaggerAppComponent.create().inject(this)
//        }


        applicationScope.launch {
            //marketPairsRepository.checkAndUpdateMarkets() // Ждем загрузки инструментов
            withContext(Dispatchers.IO) {
                marketPairsRepository.checkAndUpdateMarkets()

                combine(
                    marketPairsRepository.coinExPairs,
                    marketPairsRepository.gateIoPairs,
                    marketPairsRepository.huobiPairs
                ) { coinEx, gateIo, huobi ->
                    coinEx.isNotEmpty() && gateIo.isNotEmpty() && huobi.isNotEmpty()
                }.filter { it }.first() // Ждем, пока все списки будут непустыми

                selectedPairRepository.loadPairs()

                val selectedPairs = selectedPairRepository.selectedPairs.first()
                selectedPairRepository.savePairs(selectedPairs)


            }

            //val selected = selectedPairRepository.selectedPairs.first()
            //selectedPairRepository.savePairs(selected)

            //selectedPairRepository.loadPairs()
            // Запускаем WebSocket только после загрузки инструментов
            subscribeToCoinExUseCase.start()
            subscribeToGateIoUseCase.start()
            subscribeToHuobiUseCase.start()
            webSocketTickerRepository.collectWebSocketData()

            //delay(10000)
            //selectedPairRepository.loadPairs()
        }

    }

    override fun onTerminate() {
        super.onTerminate()
        subscribeToCoinExUseCase.stop()
        subscribeToGateIoUseCase.stop()
        subscribeToHuobiUseCase.stop()
        (applicationScope as? CoroutineScope)?.cancel() // Явная отмена скоупа
    }
}