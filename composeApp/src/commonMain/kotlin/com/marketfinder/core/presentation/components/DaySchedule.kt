package com.marketfinder.core.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.calendar.CalendarEvent

/**
 * Day Schedule - Pre-Built Template Component
 *
 * Vertical time slots (24h) with event blocks positioned by time and current time indicator.
 *
 * Note: Event times are computed in UTC by default. Pass `timeOffsetMillis` (e.g.,
 * `TimeZone.currentSystemDefault().offsetAt(Clock.System.now()).totalSeconds * 1000L`)
 * to display events in local time.
 *
 * Usage:
 * ```
 * DaySchedule(
 *     events = dayEvents,
 *     currentTimeMillis = Clock.System.now().toEpochMilliseconds(),
 *     timeOffsetMillis = localOffsetMillis,
 *     onEventClick = { event -> navController.navigate(EventDetail(event.id)) }
 * )
 * ```
 */
@Composable
fun DaySchedule(
    events: List<CalendarEvent>,
    modifier: Modifier = Modifier,
    currentTimeMillis: Long? = null,
    startHour: Int = 0,
    endHour: Int = 24,
    timeOffsetMillis: Long = 0L,
    onEventClick: ((CalendarEvent) -> Unit)? = null
) {
    val hourHeight = 60.dp
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState)
    ) {
        Box {
            // Time slots
            Column {
                for (hour in startHour until endHour) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(hourHeight)
                    ) {
                        Text(
                            text = formatHour(hour),
                            modifier = Modifier
                                .width(56.dp)
                                .padding(end = 8.dp, top = 0.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            // Event blocks
            events.forEach { event ->
                if (!event.isAllDay) {
                    val startHourOfDay = getHourOfDay(event.startMillis + timeOffsetMillis)
                    val durationHours = ((event.endMillis - event.startMillis).toFloat() / 3_600_000f)
                        .coerceAtLeast(0.5f)
                    val clampedStart = startHourOfDay.coerceIn(startHour.toFloat(), endHour.toFloat())
                    val topOffset = ((clampedStart - startHour) * hourHeight.value).dp
                    val eventHeight = (durationHours * hourHeight.value).dp

                    Surface(
                        modifier = Modifier
                            .padding(start = 64.dp, end = 8.dp)
                            .offset(y = topOffset)
                            .fillMaxWidth()
                            .height(eventHeight)
                            .let { mod ->
                                if (onEventClick != null) mod.clickable { onEventClick(event) }
                                else mod
                            },
                        shape = RoundedCornerShape(4.dp),
                        color = (event.color ?: MaterialTheme.colorScheme.primaryContainer)
                            .copy(alpha = 0.8f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = event.title,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                            if (event.location != null) {
                                Text(
                                    text = event.location,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Current time indicator
            if (currentTimeMillis != null) {
                val currentHour = getHourOfDay(currentTimeMillis + timeOffsetMillis)
                if (currentHour in startHour.toFloat()..endHour.toFloat()) {
                    val topOffset = ((currentHour - startHour) * hourHeight.value).dp
                    Box(
                        modifier = Modifier
                            .padding(start = 56.dp)
                            .offset(y = topOffset)
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(MaterialTheme.colorScheme.error)
                    )
                }
            }
        }
    }
}

private fun formatHour(hour: Int): String {
    return when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }
}

private fun getHourOfDay(millis: Long): Float {
    val totalSeconds = millis / 1000
    val secondsInDay = ((totalSeconds % 86400) + 86400) % 86400
    return secondsInDay.toFloat() / 3600f
}
