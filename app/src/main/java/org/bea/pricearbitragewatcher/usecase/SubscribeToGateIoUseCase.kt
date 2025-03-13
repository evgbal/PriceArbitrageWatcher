package org.bea.pricearbitragewatcher.usecase

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import org.bea.pricearbitragewatcher.ApplicationScope
import org.bea.pricearbitragewatcher.data.SelectedPairRepository
import org.bea.pricearbitragewatcher.net.CoinExWebSocketClient
import org.bea.pricearbitragewatcher.net.GateIoWebSocketClient
import javax.inject.Inject


//class SubscribeToGateIoUseCase @Inject constructor(
//    private val gateIoApi: GateIoApi
//) {
//
//    fun execute(subscriptionMessage: SubscriptionMessage, callback: (Result<String>) -> Unit) {
//        gateIoApi.subscribeToTickers(subscriptionMessage).enqueue(object : Callback<ResponseBody> {
//            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
//                if (response.isSuccessful) {
//                    callback(Result.success("Подписка на каналы успешно выполнена"))
//                } else {
//                    callback(Result.failure(Throwable("Ошибка подписки: ${response.errorBody()}")))
//                }
//            }
//
//            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                callback(Result.failure(t))
//            }
//        })
//    }
//}

//class SubscribeToGateIoUseCase @Inject constructor(
//    private val webSocketClient: GateIoWebSocketClient
//) {
//    fun execute(): Flow<String> = webSocketClient.connect()
//}

class SubscribeToGateIoUseCase @Inject constructor(
    private val webSocketClient: GateIoWebSocketClient,
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
                    val tickers = selectedPairRepository.getGateIoTickers()
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

    fun getMessageFlow(): SharedFlow<String> = GateIoWebSocketClient.messageFlow
}