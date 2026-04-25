package io.github.janmalch.volcanicglass.ui.screens.file

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavKey
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.State
import io.github.janmalch.shed.Shed
import io.github.janmalch.volcanicglass.R
import io.github.janmalch.volcanicglass.core.UriKSerializer
import io.github.janmalch.volcanicglass.core.content.ContentFile
import io.github.janmalch.volcanicglass.core.content.TreeState
import io.github.janmalch.volcanicglass.ui.components.FileTree
import kotlinx.collections.immutable.ImmutableList
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class FileScreen(
    @Serializable(with = UriKSerializer::class)
    val file: Uri?,
) : NavKey

@Composable
fun FileScreen(
    key: FileScreen,
    onFileClick: (TreeState.Success.Node) -> Unit,
    viewModel: FileViewModel = hiltViewModel<FileViewModel, FileViewModel.Factory>(
        creationCallback = { factory -> factory.create(key) }
    )
) {
    val file by viewModel.file.collectAsStateWithLifecycle()
    val state by viewModel.markdownFlow.collectAsStateWithLifecycle()
    val tree by viewModel.tree.collectAsStateWithLifecycle()
    val recentFiles by viewModel.recentFiles.collectAsStateWithLifecycle()
    FileScreen(file, state, tree, recentFiles, onFileClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileScreen(
    file: ContentFile?,
    state: State,
    tree: TreeState,
    recentFiles: ImmutableList<TreeState.Success.Node>,
    onFileClick: (TreeState.Success.Node) -> Unit,
) {
    var isTreeVisible by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    file?.also {
                        Text(
                            text = it.name,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = file != null && tree is TreeState.Success,
                        onClick = {
                            file ?: return@IconButton
                            val success = tree as? TreeState.Success ?: return@IconButton
                            val intent = Intent(
                                Intent.ACTION_VIEW, Uri.Builder()
                                    .scheme("obsidian")
                                    .path("open")
                                    .appendQueryParameter("vault", success.root.name)
                                    .appendQueryParameter("file", file.name)
                                    .build()
                                    .toString()
                                    .replace(":/", "://")
                                    .toUri()
                            )
                            context.startActivity(intent)
                        },
                    ) {
                        Icon(
                            Icons.Default.Create,
                            contentDescription = stringResource(R.string.open_in_obsidian)
                        )
                    }
                    // FIXME: proper menu
                    IconButton(onClick = { Shed.startActivity(context) }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = null,
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp),
                    ) {
                        val colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                        )
                        for (node in recentFiles) {
                            SuggestionChip(
                                label = { Text(node.name) },
                                onClick = { onFileClick(node) },
                                colors = colors
                            )
                        }
                    }
                },
                floatingActionButton = {
                    FloatingActionButton(onClick = {
                        isTreeVisible = true
                    }) {
                        // FIXME
                        Icon(Icons.Default.Search, contentDescription = null)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            Markdown(
                state = state,
                imageTransformer = Coil3ImageTransformer
            )
        }

        if (isTreeVisible) {
            Dialog(
                onDismissRequest = { isTreeVisible = false },
                properties = remember { DialogProperties() },
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.9f),
                ) {
                    when (tree) {
                        is TreeState.Success -> FileTree(
                            tree = tree,
                            onFileClick = {
                                isTreeVisible = false
                                onFileClick(it)
                            },
                            modifier = Modifier
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp)
                        )

                        else -> Text(tree.toString())
                    }
                }
            }
        }
    }
}
