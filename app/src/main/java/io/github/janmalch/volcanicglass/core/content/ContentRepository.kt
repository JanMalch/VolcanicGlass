package io.github.janmalch.volcanicglass.core.content

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.dataStore
import androidx.documentfile.provider.DocumentFile
import dagger.hilt.android.qualifiers.ApplicationContext
import io.github.janmalch.volcanicglass.core.ApplicationScope
import io.github.janmalch.volcanicglass.core.IoDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlin.time.measureTimedValue


sealed interface TreeState {
    data object Loading : TreeState
    data object Failure : TreeState
    data object NoVault : TreeState

    @ConsistentCopyVisibility
    data class Success internal constructor(
        val root: Node,
        private val lut: Map<Uri, Node> = emptyMap()
    ) : TreeState {

        operator fun get(uri: Uri): Node? = lut[uri]

        data class Node(
            val uri: Uri,
            val name: String,
            val isDirectory: Boolean,
            val children: List<Node>,
        ) {
            constructor(
                file: DocumentFile,
                children: List<Node>
            ) : this(file.uri, file.name ?: "?", file.isDirectory, children)
        }

        companion object {
            private fun DocumentFile.toNode(lut: MutableMap<Uri, Node>): Node {
                if (isFile) {
                    return Node(this, emptyList()).also { lut[uri] = it }
                }
                return Node(
                    this,
                    listFiles().filter { (it.isDirectory && it.name != ".obsidian") || it.type == "text/markdown" }
                        .map { it.toNode(lut) }).also { lut[uri] = it }
            }

            fun valueOf(root: DocumentFile): Success {
                val lut = mutableMapOf<Uri, Node>()
                return Success(root.toNode(lut), lut)
            }
        }
    }
}

data class ContentFile(
    val name: String,
    val content: String
)

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class ContentRepository @Inject constructor(
    @ApplicationScope private val scope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineContext,
    @ApplicationContext private val context: Context,
) {

    private val contentResolver: ContentResolver = context.contentResolver

    val hasVault =
        context.vaultStore.data.map { it.directory }.distinctUntilChanged().map { it != null }

    val tree: StateFlow<TreeState> = context.vaultStore.data
        .map { it.directory }
        .distinctUntilChanged()
        .flatMapLatest { vaultUri ->
            vaultUri ?: return@flatMapLatest flowOf(null)
            contentResolver.watch(vaultUri, true)
        }
        .map { vaultUri ->
            vaultUri ?: return@map TreeState.NoVault
            val vaultDocument = measureTimedValue {
                DocumentFile.fromTreeUri(context as Application, vaultUri)
            }.also { Timber.d("Getting document file from vault took %s.", it.duration) }
                .value
            if (vaultDocument == null) {
                Timber.e("Failed to get document file for vault.")
                return@map TreeState.Failure
            }
            measureTimedValue {
                TreeState.Success.valueOf(vaultDocument)
            }.also { Timber.d("Building document tree took %s.", it.duration) }
                .value
        }
        .catch {
            Timber.e(it, "Failed to create document tree.")
            emit(TreeState.Failure)
        }
        .flowOn(ioDispatcher)
        .stateIn(scope, SharingStarted.Eagerly, TreeState.Loading)

    val storedRecentFiles = context.historyStore.data.map { it.recents }.distinctUntilChanged()

    val recentFiles = tree.filterIsInstance<TreeState.Success>()
        .flatMapLatest { success ->
            storedRecentFiles.map { list -> list.mapNotNull { success[it] } }
        }

    fun watchFile(file: Uri): Flow<ContentFile> {
        updateHistory(file)
        return contentResolver.watch(file, false)
            .map { readFile(file) }
            .flowOn(ioDispatcher)
    }

    private fun readFile(file: Uri): ContentFile {
        val doc = checkNotNull(DocumentFile.fromSingleUri(context, file)) {
            "No file found for $file."
        }
        require(doc.isFile) { "$file does not point to a file." }
        val content = checkNotNull(contentResolver.openInputStream(file)) {
            "No file found for $file."
        }.bufferedReader().readText()
        return ContentFile(name = doc.name ?: "?", content = content)
    }


    private fun updateHistory(file: Uri) {
        scope.launch(CoroutineExceptionHandler { _, throwable ->
            Timber.e(throwable, "Failed to update history of recent files.")
        }) {
            val fileStr = file.toString()
            val updated = context.historyStore.data.first()
                .recentUris
                .toMutableList()
                .apply { remove(fileStr); add(0, fileStr) }
                .take(5)
            context.historyStore.updateData { HistoryData(updated) }
            Timber.d("Updated history: %s", updated)
        }
    }

    suspend fun setVaultUri(directory: Uri) {
        contentResolver.takePersistableUriPermission(
            directory,
            Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        context.vaultStore.updateData { VaultData(directory) }
    }
}

private val Context.vaultStore by dataStore(
    fileName = "vault.json",
    serializer = VaultDataStoreSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler {
        Timber.e(it, "Replacing corrupted file of vault store.")
        VaultDataStoreSerializer.defaultValue
    }
)


private val Context.historyStore by dataStore(
    fileName = "history.json",
    serializer = HistoryDataStoreSerializer,
    corruptionHandler = ReplaceFileCorruptionHandler {
        Timber.e(it, "Replacing corrupted file of history store.")
        HistoryDataStoreSerializer.defaultValue
    }
)
