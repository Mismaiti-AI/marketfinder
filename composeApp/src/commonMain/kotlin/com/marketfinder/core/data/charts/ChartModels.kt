package com.marketfinder.core.data.charts

import androidx.compose.ui.graphics.Color

/**
 * Chart Models - Pre-Built Template Component
 *
 * Data classes for chart rendering. Used by BarChart, LineChart, PieChart components.
 * Charts are pure UI — they render data passed from any source (Firestore, API, Room, etc.)
 */

data class ChartDataPoint(
    val label: String,
    val value: Float,
    val color: Color? = null
)

data class ChartMetrics(
    val min: Float,
    val max: Float,
    val avg: Float,
    val total: Float
) {
    companion object {
        fun from(points: List<ChartDataPoint>): ChartMetrics {
            if (points.isEmpty()) return ChartMetrics(0f, 0f, 0f, 0f)
            val values = points.map { it.value }
            return ChartMetrics(
                min = values.min(),
                max = values.max(),
                avg = values.average().toFloat(),
                total = values.sum()
            )
        }
    }
}

enum class ChartType {
    BAR, LINE, PIE, DONUT, AREA
}
