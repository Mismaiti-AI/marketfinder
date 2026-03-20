package com.marketfinder.core.presentation.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.calendar.CalendarDay
import com.marketfinder.core.data.calendar.CalendarEvent
import com.marketfinder.core.data.calendar.CalendarViewMode
import com.marketfinder.core.presentation.components.AgendaList
import com.marketfinder.core.presentation.components.CalendarEventCard
import com.marketfinder.core.presentation.components.CalendarGrid
import com.marketfinder.core.presentation.components.DaySchedule
import com.marketfinder.core.presentation.components.WeekStrip

/**
 * Generic Calendar Screen - Pre-Built Template Component
 *
 * Screen with view mode tabs (month/week/day/agenda), calendar display,
 * event list for selected day, FAB for add event, and pull-to-refresh.
 *
 * Usage:
 * ```
 * GenericCalendarScreen(
 *     title = "My Calendar",
 *     monthLabel = "January 2026",
 *     days = calendarDays,
 *     selectedDayEvents = selectedEvents,
 *     onDayClick = { day -> viewModel.selectDay(day) },
 *     onPreviousMonth = { viewModel.previousMonth() },
 *     onNextMonth = { viewModel.nextMonth() },
 *     onAddEvent = { navController.navigate(AddEvent) },
 *     onEventClick = { event -> navController.navigate(EventDetail(event.id)) },
 *     onBackClick = { navController.popBackStack() }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericCalendarScreen(
    title: String,
    monthLabel: String,
    days: List<CalendarDay>,
    selectedDayEvents: List<CalendarEvent>,
    onDayClick: (CalendarDay) -> Unit,
    modifier: Modifier = Modifier,
    dayOfWeekHeaders: List<String> = listOf("S", "M", "T", "W", "T", "F", "S"),
    onPreviousMonth: (() -> Unit)? = null,
    onNextMonth: (() -> Unit)? = null,
    onAddEvent: (() -> Unit)? = null,
    onEventClick: ((CalendarEvent) -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    currentTimeMillis: Long? = null,
    timeOffsetMillis: Long = 0L,
    agendaGroups: Map<String, List<CalendarEvent>> = emptyMap(),
    viewModes: List<CalendarViewMode> = listOf(
        CalendarViewMode.MONTH,
        CalendarViewMode.WEEK,
        CalendarViewMode.DAY,
        CalendarViewMode.AGENDA
    )
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val currentViewMode = viewModes.getOrElse(selectedTabIndex) { CalendarViewMode.MONTH }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }
                    }
                },
                actions = {
                    if (onRefresh != null) {
                        IconButton(onClick = onRefresh) {
                            Icon(Icons.Default.Refresh, "Refresh")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            if (onAddEvent != null) {
                FloatingActionButton(onClick = onAddEvent) {
                    Icon(Icons.Default.Add, "Add Event")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // View mode tabs
            if (viewModes.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 16.dp
                ) {
                    viewModes.forEachIndexed { index, mode ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    when (mode) {
                                        CalendarViewMode.MONTH -> "Month"
                                        CalendarViewMode.WEEK -> "Week"
                                        CalendarViewMode.DAY -> "Day"
                                        CalendarViewMode.AGENDA -> "Agenda"
                                    }
                                )
                            }
                        )
                    }
                }
            }

            when (currentViewMode) {
                CalendarViewMode.MONTH -> {
                    CalendarGrid(
                        monthLabel = monthLabel,
                        dayOfWeekHeaders = dayOfWeekHeaders,
                        days = days,
                        onDayClick = onDayClick,
                        onPreviousMonth = onPreviousMonth,
                        onNextMonth = onNextMonth,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Events for selected day
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        selectedDayEvents.forEach { event ->
                            CalendarEventCard(
                                event = event,
                                onClick = { onEventClick?.invoke(event) },
                                timeOffsetMillis = timeOffsetMillis
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }

                CalendarViewMode.WEEK -> {
                    // Show the 7-day window containing the selected day (or first 7 days as fallback)
                    val selectedIndex = days.indexOfFirst { it.isSelected }
                    val weekStart = if (selectedIndex >= 0) (selectedIndex / 7) * 7 else 0
                    val weekDays = days.drop(weekStart).take(7)

                    WeekStrip(
                        days = weekDays,
                        dayLabels = dayOfWeekHeaders,
                        onDayClick = onDayClick
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    DaySchedule(
                        events = selectedDayEvents,
                        currentTimeMillis = currentTimeMillis,
                        timeOffsetMillis = timeOffsetMillis,
                        onEventClick = onEventClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }

                CalendarViewMode.DAY -> {
                    DaySchedule(
                        events = selectedDayEvents,
                        currentTimeMillis = currentTimeMillis,
                        timeOffsetMillis = timeOffsetMillis,
                        onEventClick = onEventClick,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                }

                CalendarViewMode.AGENDA -> {
                    AgendaList(
                        groupedEvents = agendaGroups,
                        onEventClick = { event -> onEventClick?.invoke(event) },
                        timeOffsetMillis = timeOffsetMillis,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
