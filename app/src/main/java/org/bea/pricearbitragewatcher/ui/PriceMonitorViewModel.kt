package org.bea.pricearbitragewatcher.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.bea.pricearbitragewatcher.usecase.SubscribeToCoinExUseCase
import org.bea.pricearbitragewatcher.usecase.SubscribeToGateIoUseCase
import org.bea.pricearbitragewatcher.usecase.SubscribeToHuobiUseCase
import javax.inject.Inject

private const val TAG = "PriceMonitorViewModel"

private const val UNKNOWN_ERROR = "UNKNOWN_ERROR"

@HiltViewModel
class PriceMonitorViewModel @Inject constructor(
    //webSocketClient: GateIoWebSocketClient
    private val subscribeToGateIoUseCase: SubscribeToGateIoUseCase,
    private val subscribeToCoinExUseCase: SubscribeToCoinExUseCase,
    private val subscribeToHuobiUseCase: SubscribeToHuobiUseCase
) : ViewModel() {
    //@Inject lateinit var subscribeToGateIoUseCase: SubscribeToGateIoUseCase

    //private val subscribeToGateIoUseCase: SubscribeToGateIoUseCase

    init {
        Log.d(TAG, "init")

    }

    private val _statusGateIo = MutableStateFlow<String>("")
    val statusGateIo: StateFlow<String> get() = _statusGateIo

    fun subscribeToGateIo() {
        viewModelScope.launch {
            try {
//                subscribeToGateIoUseCase.execute()
//                    .catch { e ->
//                        withContext(Dispatchers.Main) {
//                            _statusGateIo.value = "Ошибка: ${e.message ?: UNKNOWN_ERROR}"
//                            Log.d(TAG, "GateIo .catch: ${e.stackTraceToString()}")
//                        }
//                    }
//                    .flowOn(Dispatchers.IO)
                subscribeToGateIoUseCase.getMessageFlow().collect { message ->
                        withContext(Dispatchers.Main) {
                            _statusGateIo.value = message
                            Log.d(TAG, "GateIo message: ${message}")
                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    //_uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR)
                    _statusGateIo.value = "Ошибка: ${e.message ?: UNKNOWN_ERROR}"
                    Log.d(TAG, "GateIo catch: ${e.stackTraceToString()}")
                }
            }
        }
    }

    private val _statusCoinEx = MutableStateFlow<String>("")
    val statusCoinEx: StateFlow<String> get() = _statusCoinEx

    fun subscribeToCoinEx() {
        viewModelScope.launch {
            try {
                //subscribeToCoinExUseCase.start()

//                    .catch { e ->
//                        withContext(Dispatchers.Main) {
//                            _statusCoinEx.value = "Ошибка: ${e.message ?: UNKNOWN_ERROR}"
//                            Log.d(TAG, "CoinEx .catch: ${e.stackTraceToString()}")
//                        }
//                    }
                    //                    .flowOn(Dispatchers.IO)
                subscribeToCoinExUseCase.getMessageFlow()
                    //.shareIn(viewModelScope, SharingStarted.Lazily)
                    .collect { message ->
                        //withContext(Dispatchers.IO) {
                        withContext(Dispatchers.Main) {
                            _statusCoinEx.value = message
                            Log.d(TAG, "CoinEx message: ${message}")
                        }
                        //}
                    }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    //_uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR)
                    _statusCoinEx.value = "Ошибка: ${e.message ?: UNKNOWN_ERROR}"
                    Log.d(TAG, "CoinEx catch: ${e.stackTraceToString()}")
                }
            }
        }
    }


    private val _statusHuobi = MutableStateFlow<String>("")
    val statusHuobi: StateFlow<String> get() = _statusHuobi

    fun subscribeToHuobi() {
        viewModelScope.launch {
            try {
                subscribeToHuobiUseCase.getMessageFlow()
//                    .catch { e ->
//                        withContext(Dispatchers.Main) {
//                            _statusHuobi.value = "Ошибка: ${e.message ?: UNKNOWN_ERROR}"
//                            Log.d(TAG, "Huobi .catch: ${e.stackTraceToString()}")
//                        }
//                    }
//                    .flowOn(Dispatchers.IO)
                    .collect { message ->
                        withContext(Dispatchers.Main) {
                            _statusHuobi.value = message
                            Log.d(TAG, "Huobi message: ${message}")
                        }
                    }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    //_uiState.value = UiState.Error(e.message ?: UNKNOWN_ERROR)
                    _statusHuobi.value = "Ошибка: ${e.message ?: UNKNOWN_ERROR}"
                    Log.d(TAG, "Huobi catch: ${e.stackTraceToString()}")
                }
            }
        }
    }

}

//@HiltViewModel
//class PriceMonitorViewModel @Inject constructor(
//    private val subscribeToGateIoUseCase: SubscribeToGateIoUseCase
//): ViewModel() {
//    init {
//
//    }
//
//    // Состояние подписки, используем StateFlow для управления состоянием
//    private val _status = MutableStateFlow<String>("")  // Начальное состояние
//    val status: StateFlow<String> get() = _status
//
//    // Метод подписки
//    fun subscribeToGateIo() {
//        val subscriptionMessage = SubscriptionMessage(
//            method = "sub",
//            params = Params(channel = "spot.tickers")
//        )
//
//        viewModelScope.launch {
//            subscribeToGateIoUseCase.execute(subscriptionMessage) { result ->
//                result.onSuccess { message ->
//                    _status.value = message
//                }
//                result.onFailure { throwable ->
//                    _status.value = "Ошибка: ${throwable.message}"
//                }
//            }
//        }
//    }
//}

//
//// Hilt Repository для работы с текущими данными и историей
//@Singleton
//class TickerRepository @Inject constructor(
//    private val dao: TickerHistoryDao
//) {
//
//    fun getDiscrepanciesFlow() = flow {
//        while (true) {
//            val recent = dao.getRecentTickers(System.currentTimeMillis() - DAY_IN_MILLIS)
//            emit(calculateDiscrepancies(recent))
//            delay(10_000)
//        }
//    }.flowOn(Dispatchers.IO)
//
//    suspend fun insertTicker(ticker: TickerHistoryEntity) = dao.insert(ticker)
//
//    suspend fun clearOldData() {
//        val threshold = System.currentTimeMillis() - DAY_IN_MILLIS
//        dao.deleteOld(threshold)
//    }
//}
//
//// ViewModel с Hilt
//@HiltViewModel
//class TickerViewModel @Inject constructor(
//    private val repository: TickerRepository
//) : ViewModel() {
//
//    val discrepanciesFlow = repository.getDiscrepanciesFlow()
//
//    init {
//        startWebSocket()
//    }
//
//    private fun startWebSocket() = viewModelScope.launch {
//        webSocketService.connect { tickerUpdate ->
//            repository.insertTicker(tickerUpdate)
//        }
//    }
//}
//
//// Adapter для отображения расхождений
//class DiscrepancyAdapter : ListAdapter<DiscrepancyItem, DiscrepancyAdapter.ViewHolder>(DiffCallback()) {
//
//    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        private val symbolText: TextView = view.findViewById(R.id.symbolText)
//        private val diffText: TextView = view.findViewById(R.id.diffText)
//
//        fun bind(item: DiscrepancyItem) {
//            symbolText.text = item.symbol
//            diffText.text = "${item.maxDiff}%"
//        }
//    }
//
//    class DiffCallback : DiffUtil.ItemCallback<DiscrepancyItem>() {
//        override fun areItemsTheSame(old: DiscrepancyItem, new: DiscrepancyItem) = old.symbol == new.symbol
//        override fun areContentsTheSame(old: DiscrepancyItem, new: DiscrepancyItem) = old == new
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_discrepancy, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        holder.bind(getItem(position))
//    }
//}
//
//// RecyclerView в UI
//val adapter = DiscrepancyAdapter()
//recyclerView.adapter = adapter
//
//lifecycleScope.launchWhenStarted {
//    viewModel.discrepanciesFlow.collect { list ->
//        adapter.submitList(list)
//    }
//}