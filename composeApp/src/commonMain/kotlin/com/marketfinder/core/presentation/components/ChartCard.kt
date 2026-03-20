package com.marketfinder.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.charts.ChartMetrics

/**
 * Chart Card - Pre-Built Template Component
 *
 * Wrapper card with title, subtitle, chart slot, and optional stats row.
 *
 * Usage:
 * ```
 * ChartCard(
 *     title = "Monthly Revenue",
 *     subtitle = "Last 6 months",
 *     metrics = ChartMetrics.from(data)
 * ) {
 *     BarChart(data = data, modifier = Modifier.height(180.dp))
 * }
 * ```
 */
@Composable
fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    metrics: ChartMetrics? = null,
    chart: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            chart()

            if (metrics != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MetricItem(label = "Min", value = formatMetric(metrics.min))
                    MetricItem(label = "Max", value = formatMetric(metrics.max))
                    MetricItem(label = "Avg", value = formatMetric(metrics.avg))
                    MetricItem(label = "Total", value = formatMetric(metrics.total))
                }
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun formatMetric(value: Float): String {
    return if (value == value.toLong().toFloat()) {
        value.toLong().toString()
    } else {
        val intPart = value.toLong()
        val decPart = ((value - intPart) * 10).toInt()
        val sign = if (value < 0 && intPart == 0L) "-" else ""
        "$sign$intPart.${kotlin.math.abs(decPart)}"
    }
}
