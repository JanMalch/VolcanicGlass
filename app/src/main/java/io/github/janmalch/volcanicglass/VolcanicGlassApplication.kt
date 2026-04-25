package io.github.janmalch.volcanicglass

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.gif.AnimatedImageDecoder
import coil3.gif.GifDecoder
import coil3.network.cachecontrol.CacheControlCacheStrategy
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import com.skydoves.compose.stability.runtime.ComposeStabilityAnalyzer
import com.skydoves.compose.stability.runtime.RecompositionEvent
import com.skydoves.compose.stability.runtime.RecompositionLogger
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
        ComposeStabilityAnalyzer.setEnabled(BuildConfig.DEBUG)
        if (BuildConfig.DEBUG) {
            ComposeStabilityAnalyzer.setLogger(object : RecompositionLogger {
                @SuppressLint("LogNotTimber") // doesn't belong in shed
                override fun log(event: RecompositionEvent) {
                    val message = buildString {
                        append("🔄 Recomposition #${event.recompositionCount}")
                        append(" - ${event.composableName}")
                        if (event.tag.isNotEmpty()) {
                            append(" [${event.tag}]")
                        }
                        appendLine()

                        event.parameterChanges.forEach { change ->
                            append("   • ${change.name}: ${change.type}")
                            when {
                                change.changed -> append(" ➡️ CHANGED")
                                change.stable -> append(" ✅ STABLE")
                                else -> append(" ⚠️ UNSTABLE")
                            }
                            appendLine()
                        }

                        if (event.unstableParameters.isNotEmpty()) {
                            append("   ⚠️ Unstable: ${event.unstableParameters.joinToString()}")
                        }
                    }

                    Log.v("AppRecomposition", message)
                }
            })
        }

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
