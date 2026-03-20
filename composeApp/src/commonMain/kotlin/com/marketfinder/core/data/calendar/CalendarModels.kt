package com.marketfinder.core.data.calendar

import androidx.compose.ui.graphics.Color

/**
 * Calendar Models - Pre-Built Template Component
 *
 * Data classes for calendar rendering. Used by CalendarGrid, WeekStrip,
 * DaySchedule, and AgendaList components.
 */

data class CalendarEvent(
    val id: String,
    val title: String,
    val startMillis: Long,
    val endMillis: Long,
    val description: String? = null,
    val location: String? = null,
    val color: Color? = null,
    val isAllDay: Boolean = false
)

enum class CalendarViewMode {
    MONTH, WEEK, DAY, AGENDA
}

data class CalendarDay(
    val dateMillis: Long,
    val dayOfMonth: Int,
    val isCurrentMonth: Boolean,
    val isToday: Boolean,
    val isSelected: Boolean,
    val events: List<CalendarEvent> = emptyList()
)

data class CalendarMonth(
    val year: Int,
    val month: Int,
    val days: List<CalendarDay>
)
