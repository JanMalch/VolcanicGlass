package io.github.janmalch.volcanicglass

import android.app.Application
import android.util.Log
import dagger.hilt.android.HiltAndroidApp
import io.github.janmalch.volcanicglass.core.ApplicationScope
import io.github.janmalch.volcanicglass.core.content.ContentRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.measureTime

@HiltAndroidApp
class VolcanicGlassApplication : Application() {
    @Inject
    lateinit var contentRepository: ContentRepository
    @ApplicationScope
    @Inject
    lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()

        Timber.plant(Timber.DebugTree())
        scope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Failed to prefetch recent files.")
        }) {
            measureTime {
                contentRepository.storedRecentFiles.first()
            }.also { Timber.d("Recent files prefetched within %s.", it) }
        }
    }
}
