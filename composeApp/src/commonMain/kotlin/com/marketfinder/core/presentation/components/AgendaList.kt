package com.marketfinder.core.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.calendar.CalendarEvent

/**
 * Agenda List - Pre-Built Template Component
 *
 * Date-grouped event list (today, tomorrow, upcoming).
 *
 * Usage:
 * ```
 * AgendaList(
 *     groupedEvents = mapOf(
 *         "Today" to todayEvents,
 *         "Tomorrow" to tomorrowEvents,
 *         "This Week" to weekEvents
 *     ),
 *     onEventClick = { event -> navController.navigate(EventDetail(event.id)) }
 * )
 * ```
 */
@Composable
fun AgendaList(
    groupedEvents: Map<String, List<CalendarEvent>>,
    onEventClick: (CalendarEvent) -> Unit,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    timeOffsetMillis: Long = 0L,
    emptyMessage: String = "No upcoming events"
) {
    if (groupedEvents.isEmpty() || groupedEvents.values.all { it.isEmpty() }) {
        Text(
            text = emptyMessage,
            modifier = modifier.padding(32.dp),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    LazyColumn(
        modifier = modifier,
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groupedEvents.forEach { (dateLabel, events) ->
            if (events.isNotEmpty()) {
                item(key = "header_$dateLabel") {
                    Text(
                        text = dateLabel,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                items(
                    items = events,
                    key = { it.id }
                ) { event ->
                    CalendarEventCard(
                        event = event,
                        onClick = { onEventClick(event) },
                        timeOffsetMillis = timeOffsetMillis
                    )
                }
            }
        }
    }
}
