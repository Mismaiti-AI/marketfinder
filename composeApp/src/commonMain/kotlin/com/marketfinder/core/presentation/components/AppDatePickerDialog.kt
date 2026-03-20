package com.marketfinder.core.presentation.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import kotlinx.datetime.Instant

/**
 * App Date Picker Dialog - Pre-Built Template Component
 *
 * Material3 DatePickerDialog wrapper with simple show/hide state control.
 * Returns selected date as "yyyy-MM-dd" string via onDateSelected callback.
 *
 * Usage:
 * ```
 * var showPicker by remember { mutableStateOf(false) }
 * var selectedDate by remember { mutableStateOf("") }
 *
 * AppDatePickerDialog(
 *     show = showPicker,
 *     onDateSelected = { date ->
 *         selectedDate = date
 *         showPicker = false
 *     },
 *     onDismiss = { showPicker = false }
 * )
 * ```
 */
@Composable
fun AppDatePickerDialog(
    show: Boolean,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    confirmText: String = "OK",
    dismissText: String = "Cancel",
    initialDateMillis: Long? = null
) {
    if (!show) return

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val formatted = Instant.fromEpochMilliseconds(millis)
                            .toString()
                            .substringBefore('T')
                        onDateSelected(formatted)
                    }
                }
            ) {
                Text(confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText)
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
