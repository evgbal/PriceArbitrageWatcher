package org.bea.pricearbitragewatcher

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.ar.core.Config
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.runner.RunWith
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.FragmentScenario
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasMinimumChildCount
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import org.bea.pricearbitragewatcher.ui.CurrencyPairSelectionFragment
import org.bea.pricearbitragewatcher.ui.CurrencyPairSelectionViewModel
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.bea.pricearbitragewatcher.data.ArbitrageRouteDao
import org.bea.pricearbitragewatcher.data.MarketPairsRepository
import org.bea.pricearbitragewatcher.data.SelectedPairRepository
import org.bea.pricearbitragewatcher.data.TickerDao
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnit
import org.mockito.junit.MockitoRule
import org.mockito.kotlin.whenever
import javax.inject.Inject


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@HiltAndroidTest
class CurrencyPairSelectionFragmentTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    //@get:Rule
    //val mockitoRule = MockitoRule() // Добавляем правило для Mockito

    @get:Rule
    val mockitoRule: MockitoRule = MockitoJUnit.rule() // Полный путь для MockitoRule

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    @Inject
    lateinit var viewModel: CurrencyPairSelectionViewModel

    @Inject
    lateinit var marketPairsRepository: MarketPairsRepository

    @Inject
    lateinit var selectedPairRepository: SelectedPairRepository

    @Inject
    lateinit var tickerDao: TickerDao

    @Inject
    lateinit var arbitrageRouteDao: ArbitrageRouteDao

    private lateinit var scenario: FragmentScenario<CurrencyPairSelectionFragment>

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this) // Инициализация Mockito
        hiltRule.inject()
        Dispatchers.setMain(testDispatcher)

        // Настраиваем поведение зависимостей
        whenever(marketPairsRepository.getActiveMarketPairsFlow()).thenReturn(
            MutableStateFlow(listOf("BTC/USDT", "ETH/USDT", "LTC/USDT"))
        )
        whenever(selectedPairRepository.selectedPairs).thenReturn(
            MutableStateFlow(listOf("BTC/USDT", "ETH/USDT"))
        )

        //whenever(tickerDao.()).thenReturn(/* тестовые данные */) // Настройте, если нужно

        // Создаём моки потоков
        //whenever(viewModel.filteredPairs).thenReturn(MutableStateFlow(listOf("BTC/USDT", "ETH/USDT")))
        //whenever(viewModel.selectedPairs).thenReturn(MutableStateFlow(setOf("BTC/USDT")))

        // Запускаем фрагмент
        //scenario = launchFragmentInHiltContainer<CurrencyPairSelectionFragment>()
        //scenario = FragmentScenario.launch(CurrencyPairSelectionFragment::class.java)

        scenario = launchFragmentInContainer<CurrencyPairSelectionFragment>(
            themeResId = R.style.Theme_PriceArbitrageWatcher // Укажите ваш стиль, если нужно
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        scenario.close()
    }

    @Test
    fun testRecyclerViewDisplaysPairs() = runTest {
        testDispatcher.scheduler.advanceUntilIdle() // Ждём завершения корутин
        onView(withId(R.id.recyclerView))
            .check(matches(hasMinimumChildCount(2))) // Должно быть 2 элемента
    }

    @Test
    fun testSearchFiltersPairs() = runTest {
        testDispatcher.scheduler.advanceUntilIdle() // Ждём завершения корутин
        onView(withId(R.id.searchEditText)).perform(typeText("BTC"))
        verify(viewModel).filterPairs("BTC") // Проверяем, что вызвался метод фильтрации
    }

    @Test
    fun testTogglePairUpdatesViewModel() = runTest {
        testDispatcher.scheduler.advanceUntilIdle() // Ждём завершения корутин
        onView(withText("BTC/USDT")).perform(click()) // Кликаем по Switch
        verify(viewModel).togglePair("BTC/USDT", false) // Должен сняться выбор
    }

    @Test
    fun testSelectAllButtonSelectsAllPairs() = runTest {
        testDispatcher.scheduler.advanceUntilIdle() // Ждём завершения корутин
        onView(withId(R.id.selectAllButton)).perform(click())
        verify(viewModel).selectAllFilteredPairs()
    }

    @Test
    fun testDeselectAllButtonDeselectsAllPairs() = runTest {
        testDispatcher.scheduler.advanceUntilIdle() // Ждём завершения корутин
        onView(withId(R.id.deselectAllButton)).perform(click())
        verify(viewModel).deselectAllFilteredPairs()
    }
}
