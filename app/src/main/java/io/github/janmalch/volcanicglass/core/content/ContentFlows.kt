package io.github.janmalch.volcanicglass.core.content

import android.content.ContentResolver
import android.database.ContentObserver
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onStart


internal fun ContentResolver.watch(
    uri: Uri,
    notifyForDescendants: Boolean,
): Flow<Uri> = watchChanges(uri, notifyForDescendants).onStart { emit(uri) }

internal fun ContentResolver.watchChanges(
    uri: Uri,
    notifyForDescendants: Boolean,
): Flow<Uri> =
    callbackFlow {
        // FIXME: avoid main looper?
        val handler = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Handler.createAsync(Looper.getMainLooper())
        } else {
            Handler(Looper.getMainLooper())
        }
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                trySendBlocking(uri)
            }
        }
        registerContentObserver(uri, notifyForDescendants, observer)
        awaitClose {
            unregisterContentObserver(observer)
        }
    }