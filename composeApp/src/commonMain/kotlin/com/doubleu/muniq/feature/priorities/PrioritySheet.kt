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
import androidx.compose.ui.zIndex
import com.doubleu.muniq.core.localization.Strings
import com.doubleu.muniq.core.model.MetricType
import org.koin.compose.viewmodel.koinViewModel
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrioritySheet(
    viewModel: PriorityViewModel = koinViewModel(),
    strings: Strings,
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val haptic = LocalHapticFeedback.current
    val listState = rememberLazyListState()

    // ... (Keep existing state logic) ...
    val importantItems = remember { mutableStateListOf<MetricType>() }

    LaunchedEffect(uiState.important) {
        importantItems.clear()
        importantItems.addAll(uiState.important)
    }

    val reorderableState = rememberReorderableLazyListState(listState) { from, to ->
        val fromKey = from.key as? MetricType
        val toKey = to.key as? MetricType

        if (fromKey != null && toKey != null) {
            val fromIndex = importantItems.indexOf(fromKey)
            val toIndex = importantItems.indexOf(toKey)

            if (fromIndex != -1 && toIndex != -1) {
                val item = importantItems.removeAt(fromIndex)
                importantItems.add(toIndex, item)
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
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 16.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        },
        modifier = Modifier.padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                            text = strings.priority_sheet_title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = strings.priority_sheet_description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // --- Active Section Header (Index 1) ---
                item {
                    SectionHeader(strings.priority_active_section, MaterialTheme.colorScheme.onSurfaceVariant)
                }

                if (importantItems.isEmpty()) {
                    item { EmptyState(strings.priority_empty_state) }
                }

                // --- Draggable Items ---
                items(importantItems, key = { it }) { metric ->
                    ReorderableItem(reorderableState, key = metric) { isDragging ->
                        val elevation by animateDpAsState(if (isDragging) 8.dp else 0.dp)

                        LaunchedEffect(isDragging) {
                            if (isDragging) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        }

                        ActivePriorityItem(
                            metric = metric,
                            metricName = getLocalizedMetricName(metric, strings),
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
                    SectionHeader(strings.priority_ignored_section, MaterialTheme.colorScheme.onSurfaceVariant)
                }

                items(uiState.notRelevant, key = { it }) { metric ->
                    IgnoredPriorityItem(
                        metric = metric,
                        metricName = getLocalizedMetricName(metric, strings),
                        onAdd = { viewModel.moveToImportant(metric) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }

            // Fixed Button
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
                    .padding(horizontal = 24.dp)
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Transparent, MaterialTheme.colorScheme.surface)
                        )
                    )
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(100)
                ) {
                    Text(strings.priority_update_map_button, fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
fun ActivePriorityItem(
    metric: MetricType,
    metricName: String, // Make sure to pass this if you added it, or use metric.displayName
    isDragging: Boolean,
    elevation: Dp,
    dragHandleModifier: Modifier,
    onRemove: () -> Unit
) {
    // FIX: Lift the item above others when dragging so the shadow isn't cut off
    val zIndex = if (isDragging) 1f else 0f

    Surface(
        shape = RoundedCornerShape(24.dp),
        // Use 'elevation' passed from parent for the physical shadow size
        shadowElevation = elevation,
        color = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .zIndex(zIndex)
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
                text = metricName, // Or metric.displayName
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
    metricName: String,
    onAdd: () -> Unit
) {
    Surface(
        onClick = onAdd,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
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
                text = metricName,
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

// ... (Helper functions EmojiIcon, SectionHeader, EmptyState remain the same) ...

private fun getLocalizedMetricName(type: MetricType, strings: Strings): String {
    return when (type) {
        MetricType.RENT -> strings.metric_rent
        MetricType.GREEN -> strings.metric_green
        MetricType.CHILD -> strings.metric_child
        MetricType.STUDENT -> strings.metric_student
        MetricType.QUIET -> strings.metric_quiet
        MetricType.AIR -> strings.metric_air
        MetricType.BIKE -> strings.metric_bike
        MetricType.DENSITY -> strings.metric_density
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
    }
}

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