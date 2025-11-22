package com.doubleu.muniq.feature.priorities

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import com.doubleu.muniq.core.model.MetricType
import kotlin.math.roundToInt

private fun getEmojiForMetric(type: MetricType): String {
    return when (type) {
        MetricType.RENT -> "💸"
        MetricType.GREEN -> "🌳"
        MetricType.CHILD -> "👶"
        MetricType.STUDENT -> "🎓"
        MetricType.QUIET -> "🤫"
        MetricType.AIR -> "💨"
        MetricType.BIKE -> "🚴"
        MetricType.DENSITY -> "🏙️"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySheet(
    viewModel: PriorityViewModel = viewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Scrollable content section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)  // Take remaining space
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "Your Priorities",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Long-press and drag to reorder. Higher = more important.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                Spacer(modifier = Modifier.height(24.dp))

                // Important Section with drag-and-drop
                Text(
                    text = "ACTIVE PRIORITIES",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                DraggableList(
                    items = uiState.important,
                    onReorder = { viewModel.reorderImportant(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Not Relevant Section
                Text(
                    text = "IGNORED",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
                )

                uiState.notRelevant.forEach { metric ->
                    PriorityItem(
                        metric = metric,
                        isImportant = false,
                        onClick = { viewModel.moveToImportant(metric) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Fixed button at bottom
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 16.dp)
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(100),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "Update Map",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }
            }
        }
    }
}

@Composable
fun DraggableList(
    items: List<MetricType>,
    onReorder: (List<MetricType>) -> Unit,
    modifier: Modifier = Modifier
) {
    var draggedItem by remember { mutableStateOf<MetricType?>(null) }
    var dragOffset by remember { mutableStateOf(Offset.Zero) }

    Column(modifier = modifier) {
        items.forEachIndexed { index, metric ->
            val isDragging = draggedItem == metric
            val itemHeight = 60.dp
            val itemHeightPx = with(LocalDensity.current) { itemHeight.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(itemHeight)
                    .zIndex(if (isDragging) 1f else 0f)
                    .offset {
                        if (isDragging) {
                            IntOffset(0, dragOffset.y.roundToInt())
                        } else {
                            IntOffset.Zero
                        }
                    }
                    .pointerInput(metric) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = {
                                draggedItem = metric
                                dragOffset = Offset.Zero
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                dragOffset += dragAmount

                                // Calculate new position based on drag offset
                                val targetIndex = index + (dragOffset.y / itemHeightPx).roundToInt()
                                val clampedIndex = targetIndex.coerceIn(0, items.lastIndex)

                                if (clampedIndex != index) {
                                    val newList = items.toMutableList()
                                    newList.removeAt(index)
                                    newList.add(clampedIndex, metric)
                                    onReorder(newList)
                                    dragOffset = Offset.Zero
                                }
                            },
                            onDragEnd = {
                                draggedItem = null
                                dragOffset = Offset.Zero
                            },
                            onDragCancel = {
                                draggedItem = null
dragOffset = Offset.Zero
                            }
                        )
                    }
            ) {
                PriorityItem(
                    metric = metric,
                    isImportant = true,
                    isDragging = isDragging,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun PriorityItem(
    metric: MetricType,
    isImportant: Boolean,
    isDragging: Boolean = false,
    onClick: () -> Unit
) {
    val containerColor = if (isImportant) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
    }

    val contentAlpha = if (isImportant) 1f else 0.6f
    val elevation by animateDpAsState(
        if (isDragging) 8.dp else if (isImportant) 2.dp else 0.dp,
        label = "elevation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .graphicsLayer { alpha = if (isDragging) 0.9f else contentAlpha },
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        shadowElevation = elevation,
        tonalElevation = elevation,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.surface, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = getEmojiForMetric(metric), fontSize = 20.sp)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = metric.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isImportant) FontWeight.Medium else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                imageVector = Icons.Rounded.DragHandle,
                contentDescription = "Reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}