package org.bea.pricearbitragewatcher.usecase

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.bea.pricearbitragewatcher.data.SelectedPairRepository
import org.bea.pricearbitragewatcher.net.CoinExWebSocketClient
import org.bea.pricearbitragewatcher.net.HuobiWebSocketClient
import javax.inject.Inject

class SubscribeToHuobiUseCase @Inject constructor(
    private val webSocketClient: HuobiWebSocketClient,
    private val selectedPairRepository: SelectedPairRepository,
    //@ApplicationScope
    //private val applicationScope: CoroutineScope
) {
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    fun start() {
        scope.launch {
            selectedPairRepository.selectedPairs
                //.distinctUntilChanged()
                .flowOn(Dispatchers.IO)
                .collectLatest { pairs ->
                    val tickers = selectedPairRepository.getHuobiTickers()
                    Log.d("UseCase", "Подключение для: $tickers")
                    if (tickers.isNotEmpty())
                        webSocketClient.connect(tickers).collect {}
                }
        }
    }

    fun stop() {
        job.cancel()
        webSocketClient.close()
    }

    fun getMessageFlow(): SharedFlow<String> = HuobiWebSocketClient.messageFlow
}



//class SubscribeToHuobiUseCase @Inject constructor(
//    private val webSocketClient: HuobiWebSocketClient
//) {
//    fun execute(): Flow<String> = webSocketClient.connect()
//}