package org.bea.pricearbitragewatcher

import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module // ✅ Обязательно добавляем эту аннотацию перед @TestInstallIn
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [AppModule::class]
)
object AppTestModule {

    @Provides
    @Singleton
    @ApplicationScope
    fun provideTestApplicationScope(): CoroutineScope {
        return CoroutineScope(Dispatchers.IO + SupervisorJob())
    }
}