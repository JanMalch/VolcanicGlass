package io.github.janmalch.volcanicglass.core

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.coroutines.CoroutineContext

@Qualifier
@Retention(RUNTIME)
annotation class IoDispatcher

@Qualifier
@Retention(RUNTIME)
annotation class DefaultDispatcher

@Qualifier
@Retention(RUNTIME)
annotation class ApplicationScope


@Module
@InstallIn(SingletonComponent::class)
object CoroutinesModule {
    @Provides
    @IoDispatcher
    fun providesIoDispatcher(): CoroutineContext = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun providesDefaultDispatcher(): CoroutineContext = Dispatchers.Default

    @Provides
    @ApplicationScope
    @Singleton
    fun providesApplicationScope(@DefaultDispatcher dispatcher: CoroutineContext): CoroutineScope =
        CoroutineScope(
            SupervisorJob()
                    + dispatcher
                    + CoroutineName("ApplicationScope")
        )

}