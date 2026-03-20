package com.marketfinder.core.data.gsheets

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText

/**
 * Pre-built service for fetching read-only CSV data from published Google Sheets.
 *
 * Supports:
 * - Any Google Sheets URL format (edit, pubhtml, sharing links)
 * - Multi-tab sheets via sheet name → GID discovery
 * - Fallback endpoints when primary export fails
 * - Robust CSV parsing with quoted values and escaped commas
 * - HTML error page detection
 *
 * Usage:
 * ```kotlin
 * val csv = service.fetchSheetCsv(sheetUrl, "Products")
 * val rows = service.parseCsv(csv) // List<Map<String, String>>
 * val name = rows[0]["name"] // column-based access
 * ```
 */
class GoogleSheetsService(private val httpClient: HttpClient) {

    /**
     * Fetch CSV data from a published Google Sheet tab.
     *
     * @param spreadsheetUrl Any Google Sheets URL (edit, pubhtml, sharing)
     * @param sheetName The tab/sheet name to fetch (e.g., "Products", "Events")
     * @return Raw CSV string content
     * @throws IllegalStateException if the response is HTML instead of CSV
     * @throws IllegalArgumentException if the sheet ID cannot be extracted from the URL
     */
    suspend fun fetchSheetCsv(spreadsheetUrl: String, sheetName: String): String {
        val sheetId = extractSheetId(spreadsheetUrl)
        val gid = discoverGid(sheetId, sheetName)

        // Primary: export endpoint with GID
        val csvUrl = if (gid != null) {
            "$BASE_URL/$sheetId/export?format=csv&gid=$gid"
        } else {
            "$BASE_URL/$sheetId/export?format=csv"
        }

        return try {
            val csvContent = httpClient.get(csvUrl).bodyAsText()
            validateCsvResponse(csvContent)
            csvContent
        } catch (e: Exception) {
            // Fallback: gviz endpoint
            val fallbackUrl = "$BASE_URL/$sheetId/gviz/tq?tqx=out:csv&sheet=$sheetName"
            val csvContent = httpClient.get(fallbackUrl).bodyAsText()
            validateCsvResponse(csvContent)
            csvContent
        }
    }

    /**
     * Parse CSV content into a list of row maps.
     * Each map has column headers as keys and cell values as values.
     *
     * Handles:
     * - Quoted values containing commas: `"Hello, World"`
     * - Escaped double quotes: `"She said ""hi"""`
     * - Missing trailing columns (filled with empty string)
     * - Empty rows (skipped)
     *
     * @param csvContent Raw CSV string with header row
     * @return List of maps, one per data row. Empty list if no data.
     */
    fun parseCsv(csvContent: String): List<Map<String, String>> {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val headers = parseCsvLine(lines[0])
        return lines.drop(1).mapNotNull { line ->
            val values = parseCsvLine(line)
            if (values.isEmpty()) return@mapNotNull null

            buildMap {
                for (j in headers.indices) {
                    put(headers[j], if (j < values.size) values[j] else "")
                }
            }
        }
    }

    /**
     * Parse a date string from Google Sheets into epoch milliseconds.
     * Supports common formats: yyyy-MM-dd, yyyy/MM/dd, dd/MM/yyyy, MM/dd/yyyy.
     *
     * @return Epoch milliseconds, or null if unparseable
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun parseDateToEpochMillis(dateString: String): Long? {
        if (dateString.isBlank()) return null

        val trimmed = dateString.trim()
        val parts = when {
            trimmed.contains("-") -> trimmed.split("-")
            trimmed.contains("/") -> trimmed.split("/")
            else -> return null
        }
        if (parts.size != 3) return null

        // Try each format: extract (year, month, day)
        val candidates = listOf(
            Triple(parts[0], parts[1], parts[2]), // yyyy-MM-dd or yyyy/MM/dd
            Triple(parts[2], parts[1], parts[0]), // dd/MM/yyyy
            Triple(parts[2], parts[0], parts[1])  // MM/dd/yyyy
        )

        for ((yStr, mStr, dStr) in candidates) {
            val y = yStr.toIntOrNull() ?: continue
            val m = mStr.toIntOrNull() ?: continue
            val d = dStr.toIntOrNull() ?: continue
            if (y < 1970 || y > 2100 || m !in 1..12 || d !in 1..31) continue

            // Calculate epoch millis using day count from 1970-01-01
            val daysFromEpoch = daysSinceEpoch(y, m, d)
            return daysFromEpoch * MILLIS_PER_DAY
        }
        return null
    }

    // --- Internal helpers ---

    private fun extractSheetId(url: String): String {
        for (pattern in SHEET_ID_PATTERNS) {
            val match = pattern.find(url)
            if (match != null) return match.groupValues[1]
        }
        throw IllegalArgumentException("Could not extract sheet ID from URL: $url")
    }

    private suspend fun discoverGid(sheetId: String, sheetName: String): String? {
        return try {
            val html = httpClient.get("$BASE_URL/$sheetId/gviz/tq?tqx=out:html").bodyAsText()

            // Try structured JSON-like pattern first
            val structuredPattern = Regex("""'gid':\s*'(\d+)'[^}]*?'name':\s*'([^']+)'""", RegexOption.IGNORE_CASE)
            structuredPattern.findAll(html).forEach { match ->
                if (match.groupValues[2].equals(sheetName, ignoreCase = true)) {
                    return match.groupValues[1]
                }
            }

            // Fallback: loose GID-to-name association
            val loosePattern = Regex("""\b(\d+)\b.*?${Regex.escape(sheetName)}""", RegexOption.IGNORE_CASE)
            loosePattern.find(html)?.groupValues?.get(1)
        } catch (_: Exception) {
            null // Default to first sheet
        }
    }

    private fun validateCsvResponse(content: String) {
        if (content.isEmpty() ||
            content.trimStart().startsWith("<!DOCTYPE", ignoreCase = true) ||
            content.trimStart().startsWith("<html", ignoreCase = true)
        ) {
            throw IllegalStateException("Received HTML response instead of CSV data. Ensure the sheet is published.")
        }
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var insideQuotes = false
        var i = 0

        while (i < line.length) {
            when {
                line[i] == '"' && !insideQuotes -> insideQuotes = true
                line[i] == '"' && insideQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // skip escaped quote
                    } else {
                        insideQuotes = false
                    }
                }
                line[i] == ',' && !insideQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(line[i])
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    /**
     * Calculate days since 1970-01-01 for a given date.
     * Pure Kotlin — no JVM APIs.
     */
    private fun daysSinceEpoch(year: Int, month: Int, day: Int): Long {
        // Days in each month (non-leap)
        val daysInMonth = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)

        fun isLeapYear(y: Int) = (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0)

        var totalDays = 0L

        // Full years from 1970
        for (y in 1970 until year) {
            totalDays += if (isLeapYear(y)) 366 else 365
        }

        // Full months in current year
        for (m in 1 until month) {
            totalDays += daysInMonth[m]
            if (m == 2 && isLeapYear(year)) totalDays++
        }

        // Days in current month
        totalDays += day - 1

        return totalDays
    }

    companion object {
        private const val BASE_URL = "https://docs.google.com/spreadsheets/d"
        private const val MILLIS_PER_DAY = 86_400_000L

        private val SHEET_ID_PATTERNS = listOf(
            Regex("""/spreadsheets/d/([a-zA-Z0-9-_]+)"""),
            Regex("""id=([a-zA-Z0-9-_]+)"""),
            Regex("""d/([a-zA-Z0-9-_]+)/""")
        )
    }
}
