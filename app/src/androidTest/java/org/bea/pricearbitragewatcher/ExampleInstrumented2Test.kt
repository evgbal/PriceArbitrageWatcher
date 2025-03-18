package org.bea.pricearbitragewatcher

import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.CoroutineScope
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import javax.inject.Inject

@UninstallModules(AppModule::class)
@HiltAndroidTest
class ExampleInstrumented2Test {

    @Inject
    @ApplicationScope
    lateinit var applicationScope: CoroutineScope

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun exampleTest() {
        // Тестовый код
    }
}