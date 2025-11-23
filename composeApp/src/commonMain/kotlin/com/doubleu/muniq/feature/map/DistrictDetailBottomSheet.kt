package com.doubleu.muniq.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.doubleu.muniq.core.model.District
import com.doubleu.muniq.core.model.MetricType
import com.doubleu.muniq.domain.ScoreCalculator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistrictDetailBottomSheet(
    district: District,
    importantMetrics: List<MetricType>,
    ignoredMetrics: List<MetricType>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val personalizedScore = ScoreCalculator.calculatePersonalizedScore(
        scores = district.scores,
        importantMetrics = importantMetrics,
        ignoredMetrics = ignoredMetrics
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 48.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = district.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Overall Score Display
            ScoreDisplay(
                score = personalizedScore,
                label = "Personalized Score"
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Metrics Breakdown
            Text(
                text = "Metrics Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricType.values().forEach { metricType ->
                    val score = district.scores.getScoreFor(metricType)
                    val priority = when {
                        metricType in ignoredMetrics -> MetricPriority.IGNORED
                        metricType in importantMetrics -> {
                            val position = importantMetrics.indexOf(metricType) + 1
                            MetricPriority.Important(position)
                        }
                        else -> MetricPriority.STANDARD
                    }
                    
                    MetricScoreItem(
                        metricName = metricType.displayName,
                        score = score,
                        priority = priority
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreDisplay(
    score: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = getScoreColor(score)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = getScoreLabel(score),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MetricScoreItem(
    metricName: String,
    score: Int,
    priority: MetricPriority,
    modifier: Modifier = Modifier
) {
    val isIgnored = priority is MetricPriority.IGNORED
    val alpha = if (isIgnored) 0.4f else 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = metricName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    PriorityBadge(priority)
                }
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIgnored) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = alpha)
                    } else {
                        getScoreColor(score)
                    }
                )
            }

            if (!isIgnored) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = getScoreColor(score),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PriorityBadge(
    priority: MetricPriority,
    modifier: Modifier = Modifier
) {
    when (priority) {
        is MetricPriority.Important -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    text = "Priority #${priority.position}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
        MetricPriority.STANDARD -> {
            // No badge for standard metrics
        }
        MetricPriority.IGNORED -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "Ignored",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun getScoreColor(score: Int): Color {
    return when {
        score >= 80 -> Color(0xFF4CAF50) // Green
        score >= 60 -> Color(0xFF8BC34A) // Light Green
        score >= 40 -> Color(0xFFFFC107) // Yellow/Amber
        score >= 20 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFFF44336) // Red
    }
}

private fun getScoreLabel(score: Int): String {
    return when {
        score >= 80 -> "Excellent"
        score >= 60 -> "Good"
        score >= 40 -> "Average"
        score >= 20 -> "Below Average"
        else -> "Poor"
    }
}

private sealed class MetricPriority {
    data class Important(val position: Int) : MetricPriority()
    object STANDARD : MetricPriority()
    object IGNORED : MetricPriority()
}
