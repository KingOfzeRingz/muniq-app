package com.doubleu.muniq.feature.priorities

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import com.doubleu.muniq.core.model.MetricType
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySheet(
    viewModel: PriorityViewModel = koinViewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    // FIX 1: Local Mutable State
    // We use a local list for the UI to prevent glitches during rapid dragging.
    // We sync it with the ViewModel state whenever the VM updates (e.g. adding/removing items).
    val importantItems = remember { mutableStateListOf<MetricType>() }

    LaunchedEffect(uiState.important) {
        importantItems.clear()
        importantItems.addAll(uiState.important)
    }

    // FIX 2: Robust Reordering Logic using KEYS
    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        // Instead of relying on raw indices (which include headers), we find the items
        // in our list based on their unique Keys (MetricType).
        val fromKey = from.key as? MetricType
        val toKey = to.key as? MetricType

        if (fromKey != null && toKey != null) {
            val fromIndex = importantItems.indexOf(fromKey)
            val toIndex = importantItems.indexOf(toKey)

            if (fromIndex != -1 && toIndex != -1) {
                // Move in local list first (UI updates instantly)
                val item = importantItems.removeAt(fromIndex)
                importantItems.add(toIndex, item)

                // Notify ViewModel (Data updates)
                viewModel.reorderImportant(importantItems.toList())

                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            state = listState,
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- Header (Index 0) ---
            item {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = "Your Priorities",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Drag the handle ≡ to sort. Top items matter most.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // --- Active Section Header (Index 1) ---
            item {
                SectionHeader("ACTIVE PRIORITIES", MaterialTheme.colorScheme.primary)
            }

            if (importantItems.isEmpty()) {
                item { EmptyState("Tap items below to add them here") }
            }

            // --- Draggable Items ---
            // Note: key = { it } is CRITICAL for the key-based logic above to work
            items(importantItems, key = { it }) { metric ->
                ReorderableItem(reorderableState, key = metric) { isDragging ->
                    val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                    LaunchedEffect(isDragging) {
                        if (isDragging) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    }

                    ActivePriorityItem(
                        metric = metric,
                        isDragging = isDragging,
                        elevation = elevation,
                        dragHandleModifier = Modifier.draggableHandle(),
                        onRemove = { viewModel.moveToNotRelevant(metric) }
                    )
                }
            }

            // --- Divider ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Ignored Section Header ---
            item {
                SectionHeader("IGNORED", MaterialTheme.colorScheme.onSurfaceVariant)
            }

            items(uiState.notRelevant, key = { it }) { metric ->
                IgnoredPriorityItem(
                    metric = metric,
                    onAdd = { viewModel.moveToImportant(metric) }
                )
            }
        }

        // Fixed Button
        Box(
            modifier = Modifier
                .align(Alignment.End)
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(100)
            ) {
                Text("Update Map", fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun ActivePriorityItem(
    metric: MetricType,
    isDragging: Boolean,
    elevation: Dp,
    dragHandleModifier: Modifier, // This modifier enables dragging
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDragging) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = elevation,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmojiIcon(metric)

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = metric.displayName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Rounded.Remove,
                    contentDescription = "Remove",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Apply the drag modifier ONLY to this icon
            // This ensures the rest of the row allows scrolling
            Icon(
                Icons.Rounded.DragHandle,
                contentDescription = "Reorder",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = dragHandleModifier
                    .size(32.dp)
                    .padding(4.dp)
            )
        }
    }
}

@Composable
fun IgnoredPriorityItem(
    metric: MetricType,
    onAdd: () -> Unit
) {
    Surface(
        onClick = onAdd,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            EmojiIcon(metric)
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = metric.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Rounded.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// ... (Helper functions EmojiIcon, SectionHeader, EmptyState remain the same)
@Composable
fun EmojiIcon(metric: MetricType) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .background(MaterialTheme.colorScheme.surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = getEmojiForMetric(metric), fontSize = 20.sp)
    }
}

@Composable
fun SectionHeader(text: String, color: Color) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(
                MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
    }
}

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
        else -> "❓" // Fallback
    }
}