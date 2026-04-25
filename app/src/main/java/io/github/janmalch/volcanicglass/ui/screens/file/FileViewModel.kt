package io.github.janmalch.volcanicglass.ui.screens.file

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mikepenz.markdown.model.State
import com.mikepenz.markdown.model.parseMarkdownFlow
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.janmalch.volcanicglass.core.content.ContentFile
import io.github.janmalch.volcanicglass.core.content.ContentRepository
import io.github.janmalch.volcanicglass.core.content.TreeState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import timber.log.Timber

data class MarkdownFile(
    val contentFile: ContentFile?,
    val markdown: State,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel(assistedFactory = FileViewModel.Factory::class)
class FileViewModel @AssistedInject constructor(
    @Assisted private val navArgs: FileScreen,
    private val contentRepository: ContentRepository,
) : ViewModel() {
    val file = (navArgs.file?.let { contentRepository.watchFile(it) } ?: flowOf(null))
        .catch {
            Timber.e(it, "Failed to determine file from navigation arguments.")
            emit(null)
        }
        .flatMapLatest { contentFile ->
            if (contentFile != null) {
                Timber.d("Parsing file content of '%s'.", contentFile.name)
            }
            parseMarkdownFlow(contentFile?.content.orEmpty()).map { markdown ->
                MarkdownFile(contentFile, markdown)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = MarkdownFile(null, State.Loading())
        )

    val recentFiles = contentRepository.recentFiles
        .map { it.drop(1).toImmutableList() }
        .catch { Timber.e(it, "Failed to determine recent files."); emit(persistentListOf()) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, persistentListOf())

    val tree = contentRepository.tree
        .stateIn(viewModelScope, SharingStarted.Eagerly, TreeState.Loading)


    @AssistedFactory
    interface Factory {
        fun create(navKey: FileScreen): FileViewModel
    }
}
