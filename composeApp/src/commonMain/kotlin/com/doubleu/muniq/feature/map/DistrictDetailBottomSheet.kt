package com.doubleu.muniq.feature.map

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.doubleu.muniq.core.model.District
import com.doubleu.muniq.core.model.MetricType
import com.doubleu.muniq.domain.ScoreCalculator
import com.doubleu.muniq.core.localization.Strings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistrictDetailBottomSheet(
    district: District,
    importantMetrics: List<MetricType>,
    ignoredMetrics: List<MetricType>,
    strings: Strings,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val personalizedScore = remember(district, importantMetrics, ignoredMetrics) {
        ScoreCalculator.calculatePersonalizedScore(
            scores = district.scores,
            importantMetrics = importantMetrics,
            ignoredMetrics = ignoredMetrics
        )
    }

    val metricItems = remember(importantMetrics, ignoredMetrics) {
        MetricType.values().map { metricType ->
            val priority = when {
                metricType in ignoredMetrics -> MetricPriority.IGNORED
                metricType in importantMetrics -> {
                    val position = importantMetrics.indexOf(metricType) + 1
                    MetricPriority.Important(position)
                }

                else -> MetricPriority.STANDARD
            }
            metricType to priority
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
            .padding(top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()),
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 16.dp)
                    .width(48.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 48.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Section
            item {
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
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2. Score Display
            item {
                ScoreDisplay(
                    score = personalizedScore,
                    label = strings.district_detail_personalized_score,
                    strings = strings
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 3. Metrics Title
            item {
                Text(
                    text = strings.district_detail_metrics_breakdown,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 4. Metrics List
            items(metricItems) { (metricType, priority) ->
                val score = district.scores.getScoreFor(metricType)
                MetricScoreItem(
                    metricName = getLocalizedMetricName(metricType, strings),
                    score = score,
                    priority = priority,
                    strings = strings
                )
            }
        }
    }
}

@Composable
private fun ScoreDisplay(
    score: Int,
    label: String,
    strings: Strings,
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
            text = getScoreLabel(score, strings),
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
    strings: Strings,
    modifier: Modifier = Modifier
) {
    val alpha = 1f

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    PriorityBadge(priority, strings)
                }
                Text(
                    text = score.toString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getScoreColor(score)
                )
            }

                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = getScoreColor(score),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
        }
    }
}

@Composable
private fun PriorityBadge(
    priority: MetricPriority,
    strings: Strings,
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
                    text = strings.district_detail_priority_badge.replace("%d", priority.position.toString()),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        MetricPriority.STANDARD -> {
            // No badge
        }

        MetricPriority.IGNORED -> {
            Surface(
                modifier = modifier,
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = strings.district_detail_ignored_badge,
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
        score >= 80 -> Color(0xFF4CAF50)
        score >= 60 -> Color(0xFF8BC34A)
        score >= 40 -> Color(0xFFFFC107)
        score >= 20 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

private fun getScoreLabel(score: Int, strings: Strings): String {
    return when {
        score >= 80 -> strings.score_excellent
        score >= 60 -> strings.score_good
        score >= 40 -> strings.score_average
        score >= 20 -> strings.score_below_average
        else -> strings.score_poor
    }
}

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

private sealed class MetricPriority {
    data class Important(val position: Int) : MetricPriority()
    data object STANDARD : MetricPriority()
    data object IGNORED : MetricPriority()
}