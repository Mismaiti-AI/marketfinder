package com.marketfinder.core.data.gsheets

import com.marketfinder.core.data.local.AppSettings

/**
 * Pre-built configuration for Google Sheets integration.
 *
 * Stores the spreadsheet URL in AppSettings so it persists across app restarts.
 * Supports two modes:
 * - **Hardcoded**: Set DEFAULT_SHEET_URL and the app uses it immediately
 * - **Configurable**: User enters URL via SetupScreen, stored in AppSettings
 *
 * Usage in repository:
 * ```kotlin
 * class MyRepository(
 *     private val sheetsService: GoogleSheetsService,
 *     private val sheetsConfig: GoogleSheetsConfig
 * ) {
 *     suspend fun loadData() {
 *         val url = sheetsConfig.sheetUrl ?: return
 *         val csv = sheetsService.fetchSheetCsv(url, "MySheet")
 *         val rows = sheetsService.parseCsv(csv)
 *         // map rows to domain models...
 *     }
 * }
 * ```
 */
class GoogleSheetsConfig(private val settings: AppSettings) {

    /**
     * The current spreadsheet URL. Returns stored URL, falls back to default, or null.
     */
    val sheetUrl: String?
        get() {
            val stored = settings.getString(KEY_SHEET_URL, "")
            return stored.ifBlank { DEFAULT_SHEET_URL.ifBlank { null } }
        }

    /**
     * Whether a valid sheet URL is configured (either stored or default).
     */
    val isConfigured: Boolean
        get() = !sheetUrl.isNullOrBlank()

    /**
     * Save a new spreadsheet URL.
     */
    fun setSheetUrl(url: String) {
        settings.putString(KEY_SHEET_URL, url.trim())
    }

    /**
     * Clear the stored URL (reverts to default if set).
     */
    fun clearSheetUrl() {
        settings.remove(KEY_SHEET_URL)
    }

    companion object {
        private const val KEY_SHEET_URL = "gsheets_spreadsheet_url"

        /**
         * Set this to a published Google Sheets URL for hardcoded mode.
         * Leave blank for configurable mode (user enters URL at runtime).
         */
        const val DEFAULT_SHEET_URL = ""
    }
}
