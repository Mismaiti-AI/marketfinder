package com.marketfinder.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.calendar.CalendarDay

/**
 * Calendar Grid - Pre-Built Template Component
 *
 * Month grid with day-of-week headers, 6-row grid, date cells with event dots,
 * navigation between months, today highlight, and selected state.
 *
 * Usage:
 * ```
 * CalendarGrid(
 *     monthLabel = "January 2026",
 *     dayOfWeekHeaders = listOf("S", "M", "T", "W", "T", "F", "S"),
 *     days = calendarDays,
 *     onDayClick = { day -> viewModel.selectDay(day) },
 *     onPreviousMonth = { viewModel.previousMonth() },
 *     onNextMonth = { viewModel.nextMonth() }
 * )
 * ```
 */
@Composable
fun CalendarGrid(
    monthLabel: String,
    dayOfWeekHeaders: List<String>,
    days: List<CalendarDay>,
    onDayClick: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier,
    onPreviousMonth: (() -> Unit)? = null,
    onNextMonth: (() -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Month navigation header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onPreviousMonth != null) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous month")
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Text(
                text = monthLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (onNextMonth != null) {
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next month")
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            dayOfWeekHeaders.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Calendar grid (6 rows x 7 columns)
        val rows = days.chunked(7)
        rows.forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    CalendarDayCell(
                        day = day,
                        onClick = { onDayClick(day) },
                        modifier = Modifier.weight(1f)
                    )
                }
                // Fill remaining cells if week is incomplete
                repeat(7 - week.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: CalendarDay,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = when {
        day.isSelected -> MaterialTheme.colorScheme.onPrimary
        day.isToday -> MaterialTheme.colorScheme.primary
        day.isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
    }

    val backgroundColor = when {
        day.isSelected -> MaterialTheme.colorScheme.primary
        day.isToday -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        else -> MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = day.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (day.isToday || day.isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor
        )

        // Event dots
        if (day.events.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                day.events.take(3).forEach { event ->
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(
                                event.color ?: MaterialTheme.colorScheme.primary
                            )
                    )
                }
            }
        }
    }
}
