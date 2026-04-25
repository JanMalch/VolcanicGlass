package io.github.janmalch.volcanicglass

import android.app.Application
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import dagger.hilt.android.HiltAndroidApp
import io.github.janmalch.volcanicglass.core.ApplicationScope
import io.github.janmalch.volcanicglass.core.content.ContentRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import timber.log.Timber
import javax.inject.Inject
import kotlin.time.measureTime

@HiltAndroidApp
class VolcanicGlassApplication : Application(), SingletonImageLoader.Factory {
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

    @OptIn(ExperimentalCoilApi::class)
    override fun newImageLoader(context: Context): ImageLoader = ImageLoader.Builder(context)
        .components {
            add(
                OkHttpNetworkFetcherFactory(
                    cacheStrategy = { CacheControlCacheStrategy() },
                    callFactory = { OkHttpClient() },
                )
            )
            add(SvgDecoder.Factory())
            if (SDK_INT >= 28) {
                add(AnimatedImageDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }
        .build()
}
