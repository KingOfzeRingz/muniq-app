package com.doubleu.muniq.feature.priorities

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import com.doubleu.muniq.core.model.MetricType

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
    viewModel: PriorityViewModel = koinViewModel(),
    onDismiss: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        // FIX 2: Use LazyColumn for the ENTIRE content.
        // This enables nested scrolling so the sheet doesn't close when you scroll up.
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 100.dp), // Space for fixed button
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // --- Header ---
            item {
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    Text(
                        text = "Your Priorities",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap items to move them. Reorder active priorities.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // --- Active Section ---
            item {
                SectionHeader("ACTIVE PRIORITIES", MaterialTheme.colorScheme.primary)
            }

            if (uiState.important.isEmpty()) {
                item { EmptyState("Tap items below to add them here") }
            }

            items(uiState.important) { metric ->
                ActivePriorityItem(
                    metric = metric,
                    isFirst = uiState.important.first() == metric,
                    isLast = uiState.important.last() == metric,
                    onMoveUp = { viewModel.moveUp(metric) },
                    onMoveDown = { viewModel.moveDown(metric) },
                    onRemove = { viewModel.moveToNotRelevant(metric) }
                )
            }

            // --- Divider ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(16.dp))
            }

            // --- Ignored Section ---
            item {
                SectionHeader("IGNORED", MaterialTheme.colorScheme.onSurfaceVariant)
            }

            items(uiState.notRelevant) { metric ->
                IgnoredPriorityItem(
                    metric = metric,
                    onAdd = { viewModel.moveToImportant(metric) }
                )
            }
        }

        // Fixed Button at bottom (overlaid)
        Box(
            modifier = Modifier
                .align(Alignment.End) // Align to bottom of Box scope if this was a Box
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
    isFirst: Boolean,
    isLast: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        shadowElevation = 2.dp
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

            // Reorder Controls (Simpler and more reliable than drag in a scrollable sheet)
            Row {
                if (!isFirst) {
                    IconButton(onClick = onMoveUp, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.ArrowUpward, null, modifier = Modifier.size(20.dp))
                    }
                }
                if (!isLast) {
                    IconButton(onClick = onMoveDown, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Rounded.ArrowDownward, null, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Rounded.Remove, null, modifier = Modifier.size(20.dp))
                }
            }
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
        border = null
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