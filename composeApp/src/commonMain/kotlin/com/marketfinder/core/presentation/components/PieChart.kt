package com.marketfinder.core.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.charts.ChartDataPoint

/**
 * Pie/Donut Chart - Pre-Built Template Component
 *
 * Pie or donut chart with legend and animated sweep.
 *
 * Usage:
 * ```
 * PieChart(
 *     data = listOf(
 *         ChartDataPoint("Category A", 40f, Color.Blue),
 *         ChartDataPoint("Category B", 30f, Color.Green),
 *         ChartDataPoint("Category C", 30f, Color.Red)
 *     )
 * )
 * ```
 */

private val defaultChartColors = listOf(
    Color(0xFF6200EE),
    Color(0xFF03DAC5),
    Color(0xFFFF6D00),
    Color(0xFF304FFE),
    Color(0xFFD50000),
    Color(0xFF00C853),
    Color(0xFFAA00FF),
    Color(0xFFFFAB00)
)

@Composable
fun PieChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    isDonut: Boolean = false,
    donutStrokeWidth: Dp = 40.dp,
    showLegend: Boolean = true,
    animate: Boolean = true,
    centerContent: (@Composable () -> Unit)? = null
) {
    if (data.isEmpty()) return

    val total = data.sumOf { it.value.toDouble() }.toFloat()
    if (total <= 0f) return

    val animationProgress = remember { Animatable(if (animate) 0f else 1f) }
    LaunchedEffect(data) {
        if (animate) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(1f, animationSpec = tween(1000))
        }
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier.size(200.dp)
            ) {
                val canvasSize = size.minDimension
                val strokeWidthPx = if (isDonut) donutStrokeWidth.toPx() else 0f
                val arcSize = if (isDonut) {
                    Size(canvasSize - strokeWidthPx, canvasSize - strokeWidthPx)
                } else {
                    Size(canvasSize, canvasSize)
                }
                val topLeft = if (isDonut) {
                    Offset(strokeWidthPx / 2, strokeWidthPx / 2)
                } else {
                    Offset.Zero
                }

                var startAngle = -90f
                data.forEachIndexed { index, point ->
                    val sweepAngle = (point.value / total) * 360f * animationProgress.value
                    val color = point.color ?: defaultChartColors[index % defaultChartColors.size]

                    if (isDonut) {
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidthPx)
                        )
                    } else {
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = topLeft,
                            size = arcSize
                        )
                    }
                    startAngle += sweepAngle
                }
            }

            if (isDonut && centerContent != null) {
                centerContent()
            }
        }

        // Legend
        if (showLegend) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                data.forEachIndexed { index, point ->
                    val color = point.color ?: defaultChartColors[index % defaultChartColors.size]
                    val percentage = (point.value / total * 100).toInt()
                    LegendItem(
                        color = color,
                        label = point.label,
                        value = "${percentage}%"
                    )
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Canvas(modifier = Modifier.size(12.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
