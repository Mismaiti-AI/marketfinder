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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.marketfinder.core.data.charts.ChartDataPoint

/**
 * Line Chart - Pre-Built Template Component
 *
 * Line chart with gradient fill, data points, and grid lines.
 *
 * Usage:
 * ```
 * LineChart(
 *     data = listOf(
 *         ChartDataPoint("Mon", 10f),
 *         ChartDataPoint("Tue", 25f),
 *         ChartDataPoint("Wed", 18f)
 *     ),
 *     modifier = Modifier.fillMaxWidth().height(200.dp)
 * )
 * ```
 */
@Composable
fun LineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    fillColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
    gridLineColor: Color = MaterialTheme.colorScheme.outlineVariant,
    labelColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    pointColor: Color = MaterialTheme.colorScheme.primary,
    showGridLines: Boolean = true,
    showDataPoints: Boolean = true,
    showFill: Boolean = true,
    gridLineCount: Int = 4,
    animate: Boolean = true
) {
    if (data.size < 2) return

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
            val minValue = data.minOf { it.value }
            val range = if (maxValue == minValue) 1f else maxValue - minValue

            val leftPadding = 40f
            val bottomPadding = 4f
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
                    val gridValue = (minValue + range * i / gridLineCount).toInt().toString()
                    drawText(
                        textMeasurer = textMeasurer,
                        text = gridValue,
                        topLeft = Offset(0f, y - 6f),
                        style = labelStyle
                    )
                }
            }

            // Calculate points
            val points = data.mapIndexed { index, point ->
                val x = leftPadding + (chartWidth * index / (data.size - 1))
                val y = chartHeight - ((point.value - minValue) / range * chartHeight * animationProgress.value)
                Offset(x, y)
            }

            // Gradient fill
            if (showFill && points.size >= 2) {
                val fillPath = Path().apply {
                    moveTo(points.first().x, chartHeight)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, chartHeight)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(fillColor, fillColor.copy(alpha = 0f)),
                        startY = 0f,
                        endY = chartHeight
                    )
                )
            }

            // Line
            val linePath = Path().apply {
                points.forEachIndexed { index, point ->
                    if (index == 0) moveTo(point.x, point.y)
                    else lineTo(point.x, point.y)
                }
            }
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 3f)
            )

            // Data points
            if (showDataPoints) {
                points.forEach { point ->
                    drawCircle(
                        color = pointColor,
                        radius = 5f,
                        center = point
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3f,
                        center = point
                    )
                }
            }
        }

        // Bottom labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 40.dp),
            horizontalArrangement = Arrangement.SpaceBetween
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
