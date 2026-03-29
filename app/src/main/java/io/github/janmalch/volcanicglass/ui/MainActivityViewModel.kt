package io.github.janmalch.volcanicglass.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.volcanicglass.core.content.ContentRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    contentRepository: ContentRepository
) : ViewModel() {

    val state = combine(
        contentRepository.hasVault,
        contentRepository.storedRecentFiles.map { it.firstOrNull() },
    ) { hasVault, mostRecent ->
        MainActivityViewState.Ready(hasVault, mostRecent).also {
            Log.d("MainActivityViewModel", "MainActivity state is ready: $it")
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, MainActivityViewState.Loading)
}

sealed interface MainActivityViewState {
    data object Loading : MainActivityViewState
    data class Ready(val hasVault: Boolean, val mostRecent: Uri?) : MainActivityViewState
}
