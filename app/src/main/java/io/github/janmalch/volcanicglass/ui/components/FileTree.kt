package io.github.janmalch.volcanicglass.ui.components

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.janmalch.volcanicglass.core.content.TreeState
import io.github.janmalch.volcanicglass.ui.theme.VolcanicGlassTheme

@Composable
fun FileTree(
    tree: TreeState.Success,
    onFileClick: (TreeState.Success.Node) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        FileTreeNode(tree.root, onFileClick, 0.dp)
    }
}

@Composable
private fun ColumnScope.FileTreeNode(
    node: TreeState.Success.Node,
    onFileClick: (TreeState.Success.Node) -> Unit,
    paddingStart: Dp,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .run {
                if (node.isDirectory) this
                else clickable { onFileClick(node) }
            }
            .padding(start = paddingStart)
    ) {
        if (node.isDirectory) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
        }
        Text(text = node.name)
    }
    for (child in node.children) {
        FileTreeNode(child, onFileClick, paddingStart = paddingStart + 28.dp)
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
                    children = listOf(
                        TreeState.Success.Node(
                            uri = Uri.EMPTY,
                            isDirectory = true,
                            name = "Folder A",
                            children = listOf(
                                TreeState.Success.Node(
                                    uri = Uri.EMPTY,
                                    isDirectory = false,
                                    name = "File A-A",
                                    children = emptyList()
                                ),
                                TreeState.Success.Node(
                                    uri = Uri.EMPTY,
                                    isDirectory = false,
                                    name = "File A-B",
                                    children = emptyList()
                                ),
                            )
                        ),
                        TreeState.Success.Node(
                            uri = Uri.EMPTY,
                            isDirectory = false,
                            name = "File B",
                            children = emptyList()
                        )
                    )
                )
            )
        )
    }
}