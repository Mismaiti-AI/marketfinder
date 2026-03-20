package com.marketfinder.core.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marketfinder.core.data.charts.ChartDataPoint

/**
 * Bar Chart - Pre-Built Template Component
 *
 * Vertical bar chart with labels, grid lines, and animated bars.
 *
 * Usage:
 * ```
 * BarChart(
 *     data = listOf(
 *         ChartDataPoint("Jan", 100f),
 *         ChartDataPoint("Feb", 150f),
 *         ChartDataPoint("Mar", 120f)
 *     ),
 *     modifier = Modifier.fillMaxWidth().height(200.dp)
 * )
 * ```
 */
@Composable
fun BarChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    gridLineColor: Color = MaterialTheme.colorScheme.outlineVariant,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    showGridLines: Boolean = true,
    gridLineCount: Int = 4,
    animate: Boolean = true
) {
    if (data.isEmpty()) return

    val animationProgress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(data) {
        if (animate) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(1f, animationSpec = tween(800))
        }
    }

    val textMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = 10.sp, color = labelColor)

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            val maxValue = data.maxOf { it.value }
            if (maxValue <= 0f) return@Canvas

            val leftPadding = 40f
            val bottomPadding = 24f
            val chartWidth = size.width - leftPadding
            val chartHeight = size.height - bottomPadding

            // Grid lines
            if (showGridLines) {
                for (i in 0..gridLineCount) {
                    val y = chartHeight - (chartHeight * i / gridLineCount)
                    drawLine(
                        color = gridLineColor,
                        start = Offset(leftPadding, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f
                    )
                    // Grid value labels
                    val gridValue = (maxValue * i / gridLineCount).toInt().toString()
                    drawText(
                        textMeasurer = textMeasurer,
                        text = gridValue,
                        topLeft = Offset(0f, y - 6f),
                        style = labelStyle
                    )
                }
            }

            // Bars
            val barSpacing = 8f
            val totalSpacing = barSpacing * (data.size + 1)
            val barWidth = ((chartWidth - totalSpacing) / data.size).coerceAtLeast(1f)

            data.forEachIndexed { index, point ->
                val barHeight = (point.value / maxValue) * chartHeight * animationProgress.value
                val x = leftPadding + barSpacing + index * (barWidth + barSpacing)
                val y = chartHeight - barHeight

                drawRect(
                    color = point.color ?: barColor,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
            }
        }

        // Bottom labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            data.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = labelColor,
                    maxLines = 1
                )
            }
        }
    }
}
