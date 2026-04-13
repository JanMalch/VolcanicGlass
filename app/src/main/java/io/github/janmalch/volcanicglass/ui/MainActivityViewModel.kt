package io.github.janmalch.volcanicglass.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.volcanicglass.core.content.ContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    contentRepository: ContentRepository
) : ViewModel() {

    val state = combine(
        contentRepository.hasVault,
        contentRepository.storedRecentFiles.map { it.firstOrNull() }.catch {
            Timber.w(it, "Failed to determine most recent file.")
            emit(null)
        },
    ) { hasVault, mostRecent ->
        MainActivityViewState.Ready(hasVault, mostRecent).also {
            Timber.d("MainActivity state is ready: %s", it)
        } as MainActivityViewState
    }.catch {
        Timber.e(it, "Failed to determine initial app state.")
        emit(MainActivityViewState.Failure)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainActivityViewState.Loading)
}

sealed interface MainActivityViewState {
    data object Loading : MainActivityViewState
    data object Failure : MainActivityViewState
    data class Ready(val hasVault: Boolean, val mostRecent: Uri?) : MainActivityViewState
}
