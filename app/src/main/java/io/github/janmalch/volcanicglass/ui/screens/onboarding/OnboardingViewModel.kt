package io.github.janmalch.volcanicglass.ui.screens.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.volcanicglass.core.content.ContentRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
) : ViewModel() {

    fun setDirectory(uri: Uri) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Failed to set vault directory.")
        }) { contentRepository.setVaultUri(uri) }
    }
}
