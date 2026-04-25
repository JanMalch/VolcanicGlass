package io.github.janmalch.volcanicglass.ui.components

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.janmalch.volcanicglass.core.content.TreeState
import io.github.janmalch.volcanicglass.ui.theme.VolcanicGlassTheme
import kotlinx.collections.immutable.persistentListOf

@Composable
fun FileTree(
    tree: TreeState.Success,
    onFileClick: (TreeState.Success.Node) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        for (node in tree.root.children) {
            FileTreeNode(node, onFileClick, 0.dp)
        }
    }
}

@Composable
private fun ColumnScope.FileTreeNode(
    node: TreeState.Success.Node,
    onFileClick: (TreeState.Success.Node) -> Unit,
    paddingStart: Dp,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val iconRotation by animateFloatAsState(
        if (isExpanded) 0f else -90f,
        label = "icon_rotation__${node.name}"
    )
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .run {
                if (node.isDirectory) clickable { isExpanded = !isExpanded }
                else clickable { onFileClick(node) }
            }
            .padding(start = paddingStart)
    ) {
        if (node.isDirectory) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.rotate(iconRotation)
            )
        }
        Text(text = node.name)
    }
    if (isExpanded) {
    for (child in node.children) {
        FileTreeNode(child, onFileClick, paddingStart = paddingStart + 28.dp)
    }
    }
}

@Preview(showBackground = true)
@Composable
private fun FileTreePreview() {
    VolcanicGlassTheme {
        FileTree(
            onFileClick = {},
            tree = TreeState.Success(
                root = TreeState.Success.Node(
                    uri = Uri.EMPTY,
                    isDirectory = true,
                    name = "Root",
                    children = persistentListOf(
                        TreeState.Success.Node(
                            uri = Uri.EMPTY,
                            isDirectory = true,
                            name = "Folder A",
                            children = persistentListOf(
                                TreeState.Success.Node(
                                    uri = Uri.EMPTY,
                                    isDirectory = false,
                                    name = "File A-A",
                                    children = persistentListOf()
                                ),
                                TreeState.Success.Node(
                                    uri = Uri.EMPTY,
                                    isDirectory = false,
                                    name = "File A-B",
                                    children = persistentListOf()
                                ),
                            )
                        ),
                        TreeState.Success.Node(
                            uri = Uri.EMPTY,
                            isDirectory = false,
                            name = "File B",
                            children = persistentListOf()
                        )
                    )
                )
            )
        )
    }
}