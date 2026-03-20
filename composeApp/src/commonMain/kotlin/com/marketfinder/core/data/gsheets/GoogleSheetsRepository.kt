package com.marketfinder.core.data.gsheets

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.ExperimentalTime

/**
 * Pre-built abstract base for repositories that read from published Google Sheets.
 *
 * Provides:
 * - Thread-safe data loading with Mutex
 * - StateFlow-based state management (items, isLoading, error)
 * - CSV → domain model mapping via abstract [mapRow]
 * - Graceful error handling (invalid rows skipped, never crashes)
 *
 * Subclass example:
 * ```kotlin
 * class EventRepository(
 *     sheetsService: GoogleSheetsService,
 *     sheetsConfig: GoogleSheetsConfig
 * ) : GoogleSheetsRepository<Event>(sheetsService, sheetsConfig) {
 *
 *     override val sheetTabName = "Events"
 *
 *     override fun mapRow(row: Map<String, String>): Event? {
 *         return Event(
 *             id = row["id"] ?: return null,
 *             title = row["title"] ?: "",
 *             date = row["date"]?.let { parseDateToInstant(it) }
 *         )
 *     }
 * }
 * ```
 */
@OptIn(ExperimentalTime::class)
abstract class GoogleSheetsRepository<T>(
    private val sheetsService: GoogleSheetsService,
    private val sheetsConfig: GoogleSheetsConfig
) {
    private val mutex = Mutex()

    private val _items = MutableStateFlow<List<T>>(emptyList())
    val items: StateFlow<List<T>> = _items

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    /**
     * The Google Sheets tab name to fetch (e.g., "Products", "Events").
     */
    abstract val sheetTabName: String

    /**
     * Map a single CSV row (column → value) to a domain model.
     * Return null to skip invalid/incomplete rows.
     */
    abstract fun mapRow(row: Map<String, String>): T?

    /**
     * Fetch data from Google Sheets, parse, and update state.
     * Call this from ViewModel via use case.
     */
    suspend fun loadFromSheet() {
        val url = sheetsConfig.sheetUrl
        if (url.isNullOrBlank()) {
            _error.value = "No Google Sheets URL configured"
            return
        }

        mutex.withLock {
            try {
                _isLoading.value = true
                _error.value = null

                val csvContent = sheetsService.fetchSheetCsv(url, sheetTabName)
                val csvRows = sheetsService.parseCsv(csvContent)

                val mapped = csvRows.mapNotNull { row ->
                    try {
                        mapRow(row)
                    } catch (_: Exception) {
                        null // Skip invalid rows
                    }
                }

                _items.value = mapped
                onDataLoaded(mapped)
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load $sheetTabName from Google Sheets"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Refresh data (alias for loadFromSheet).
     */
    suspend fun refresh() = loadFromSheet()

    /**
     * Override to persist loaded data to Room or perform post-load actions.
     * Default implementation does nothing.
     */
    protected open suspend fun onDataLoaded(items: List<T>) {}

    /**
     * Helper: parse a date string to kotlin.time.Instant.
     * Supports: yyyy-MM-dd, yyyy/MM/dd, dd/MM/yyyy, MM/dd/yyyy.
     * Returns null if unparseable.
     */
    protected fun parseDateToInstant(dateString: String): kotlin.time.Instant? {
        val millis = sheetsService.parseDateToEpochMillis(dateString) ?: return null
        return kotlin.time.Instant.fromEpochMilliseconds(millis)
    }

    /**
     * Helper: parse a boolean string ("true", "yes", "1" → true).
     */
    protected fun parseBoolean(value: String?): Boolean {
        return value?.trim()?.lowercase() in listOf("true", "yes", "1")
    }
}
