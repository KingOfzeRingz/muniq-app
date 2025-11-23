package sh.calvin.reorderable

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

class ReorderableLazyListState

data class ReorderableItemPosition(val key: Any?)

@Composable
fun rememberReorderableLazyListState(
    listState: LazyListState,
    onMove: (from: ReorderableItemPosition, to: ReorderableItemPosition) -> Unit
): ReorderableLazyListState = remember { ReorderableLazyListState() }

@Composable
fun ReorderableItem(
    state: ReorderableLazyListState,
    key: Any?,
    content: @Composable (isDragging: Boolean) -> Unit
) {
    content(false)
}

fun Modifier.draggableHandle(): Modifier = this

