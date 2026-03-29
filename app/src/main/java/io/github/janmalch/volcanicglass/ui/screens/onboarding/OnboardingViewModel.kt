package io.github.janmalch.volcanicglass.ui.screens.onboarding

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.volcanicglass.core.content.ContentRepository
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
) : ViewModel() {

    fun setDirectory(uri: Uri) {
        viewModelScope.launch { contentRepository.setVaultUri(uri) }
    }
}
