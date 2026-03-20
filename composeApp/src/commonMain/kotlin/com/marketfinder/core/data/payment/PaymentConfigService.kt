package com.marketfinder.core.data.payment

import co.touchlab.kermit.Logger
import com.marketfinder.core.data.firestore.FirestoreService
import com.marketfinder.core.data.local.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

class PaymentConfigService(
    private val firestoreService: FirestoreService,
    private val appSettings: AppSettings
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _config = MutableStateFlow(loadCachedConfig())
    val config: StateFlow<PaymentConfig> = _config.asStateFlow()

    init {
        // Start observing Firestore for real-time config updates
        startObserving()
    }

    private fun startObserving() {
        firestoreService.observeDocument("config", "payment")
            .map { doc -> doc?.toPaymentConfig() ?: PaymentConfig() }
            .onEach { config ->
                cacheConfig(config)
                _config.value = config
            }
            .catch { e ->
                Logger.e("PaymentConfig") { "Observe failed: ${e.message}" }
            }
            .launchIn(scope)
    }

    fun observeConfig(): Flow<PaymentConfig> {
        return config
    }

    suspend fun refreshConfig(): PaymentConfig {
        return try {
            val doc = firestoreService.getDocument("config", "payment")
            val config = doc?.toPaymentConfig() ?: PaymentConfig()
            cacheConfig(config)
            _config.value = config
            config
        } catch (e: Exception) {
            Logger.e("PaymentConfig") { "Failed to refresh config: ${e.message}" }
            _config.value
        }
    }

    fun getApiKey(platform: String): String {
        val keys = _config.value.revenueCatApiKey
        return when (platform.lowercase()) {
            "android" -> keys.android
            "ios" -> keys.ios
            else -> keys.android
        }
    }

    fun isFeatureEnabled(featureId: String, activeEntitlements: Set<String>): Boolean {
        val mapping = _config.value.entitlementMapping
        return activeEntitlements.any { entitlement ->
            mapping[entitlement]?.contains(featureId) == true
        }
    }

    private fun loadCachedConfig(): PaymentConfig {
        val json = appSettings.getString(CACHE_KEY, "")
        if (json.isBlank()) return PaymentConfig()

        return try {
            parseConfigJson(json)
        } catch (e: Exception) {
            Logger.e("PaymentConfig") { "Failed to load cached config: ${e.message}" }
            PaymentConfig()
        }
    }

    private fun cacheConfig(config: PaymentConfig) {
        try {
            appSettings.putString(CACHE_KEY, serializeConfig(config))
        } catch (e: Exception) {
            Logger.e("PaymentConfig") { "Failed to cache config: ${e.message}" }
        }
    }

    private fun serializeConfig(config: PaymentConfig): String {
        val sb = StringBuilder()
        sb.append("{")
        sb.append("\"android\":\"${config.revenueCatApiKey.android}\",")
        sb.append("\"ios\":\"${config.revenueCatApiKey.ios}\",")
        sb.append("\"title\":\"${escapeJson(config.paywall.title)}\",")
        sb.append("\"subtitle\":\"${escapeJson(config.paywall.subtitle)}\",")
        sb.append("\"ctaText\":\"${escapeJson(config.paywall.ctaText)}\",")
        sb.append("\"restoreText\":\"${escapeJson(config.paywall.restoreText)}\",")
        sb.append("\"features\":[${config.paywall.features.joinToString(",") { "\"${escapeJson(it)}\"" }}],")
        sb.append("\"defaultOfferingId\":\"${escapeJson(config.offeringDisplay.defaultOfferingId)}\",")
        sb.append("\"productOrder\":[${config.offeringDisplay.productOrder.joinToString(",") { "\"${escapeJson(it)}\"" }}],")
        sb.append("\"productLabels\":{${config.offeringDisplay.productLabels.entries.joinToString(",") { "\"${escapeJson(it.key)}\":\"${escapeJson(it.value)}\"" }}},")
        sb.append("\"entitlementMapping\":{${config.entitlementMapping.entries.joinToString(",") { "\"${escapeJson(it.key)}\":[${it.value.joinToString(",") { v -> "\"${escapeJson(v)}\"" }}]" }}}")
        sb.append("}")
        return sb.toString()
    }

    private fun parseConfigJson(json: String): PaymentConfig {
        val android = extractStringValue(json, "android")
        val ios = extractStringValue(json, "ios")
        val title = extractStringValue(json, "title").ifBlank { "Upgrade to Pro" }
        val subtitle = extractStringValue(json, "subtitle").ifBlank { "Unlock all features" }
        val ctaText = extractStringValue(json, "ctaText").ifBlank { "Subscribe Now" }
        val restoreText = extractStringValue(json, "restoreText").ifBlank { "Restore Purchases" }
        val features = extractStringArray(json, "features")
        val defaultOfferingId = extractStringValue(json, "defaultOfferingId").ifBlank { "default" }
        val productOrder = extractStringArray(json, "productOrder")
        val productLabels = extractStringMap(json, "productLabels")
        val entitlementMapping = extractEntitlementMapping(json)

        return PaymentConfig(
            revenueCatApiKey = PlatformApiKey(android = android, ios = ios),
            paywall = PaywallConfig(
                title = title,
                subtitle = subtitle,
                features = features,
                ctaText = ctaText,
                restoreText = restoreText
            ),
            entitlementMapping = entitlementMapping,
            offeringDisplay = OfferingDisplayConfig(
                defaultOfferingId = defaultOfferingId,
                productOrder = productOrder,
                productLabels = productLabels
            )
        )
    }

    private fun extractStringValue(json: String, key: String): String {
        val pattern = "\"$key\":\""
        val start = json.indexOf(pattern)
        if (start == -1) return ""
        val valueStart = start + pattern.length
        val valueEnd = findUnescapedQuote(json, valueStart)
        if (valueEnd == -1) return ""
        return unescapeJson(json.substring(valueStart, valueEnd))
    }

    private fun findUnescapedQuote(json: String, from: Int): Int {
        var i = from
        while (i < json.length) {
            if (json[i] == '"') {
                // Count preceding backslashes
                var backslashes = 0
                var j = i - 1
                while (j >= from && json[j] == '\\') {
                    backslashes++
                    j--
                }
                // Quote is escaped only if preceded by odd number of backslashes
                if (backslashes % 2 == 0) return i
            }
            i++
        }
        return -1
    }

    private fun extractStringArray(json: String, key: String): List<String> {
        val pattern = "\"$key\":["
        val start = json.indexOf(pattern)
        if (start == -1) return emptyList()
        val arrayStart = start + pattern.length
        val arrayEnd = findMatchingBracket(json, arrayStart - 1, '[', ']')
        if (arrayEnd == -1) return emptyList()
        val arrayContent = json.substring(arrayStart, arrayEnd)
        if (arrayContent.isBlank()) return emptyList()
        return splitJsonArrayValues(arrayContent)
    }

    private fun extractStringMap(json: String, key: String): Map<String, String> {
        val pattern = "\"$key\":{"
        val start = json.indexOf(pattern)
        if (start == -1) return emptyMap()
        val objStart = start + pattern.length
        val objEnd = findMatchingBracket(json, objStart - 1, '{', '}')
        if (objEnd == -1) return emptyMap()
        val objContent = json.substring(objStart, objEnd)
        if (objContent.isBlank()) return emptyMap()
        return parseKeyValuePairs(objContent)
    }

    private fun extractEntitlementMapping(json: String): Map<String, List<String>> {
        val pattern = "\"entitlementMapping\":{"
        val start = json.indexOf(pattern)
        if (start == -1) return emptyMap()
        val objStart = start + pattern.length
        val objEnd = findMatchingBracket(json, objStart - 1, '{', '}')
        if (objEnd == -1) return emptyMap()
        val objContent = json.substring(objStart, objEnd)
        if (objContent.isBlank()) return emptyMap()

        val result = mutableMapOf<String, List<String>>()
        // Parse entries like "pro":["f1","f2"],"basic":["f3"]
        var pos = 0
        while (pos < objContent.length) {
            val keyStart = objContent.indexOf('"', pos)
            if (keyStart == -1) break
            val keyEnd = findUnescapedQuote(objContent, keyStart + 1)
            if (keyEnd == -1) break
            val entitlementKey = unescapeJson(objContent.substring(keyStart + 1, keyEnd))

            val arrStart = objContent.indexOf('[', keyEnd)
            if (arrStart == -1) break
            val arrEnd = findMatchingBracket(objContent, arrStart, '[', ']')
            if (arrEnd == -1) break
            val arrContent = objContent.substring(arrStart + 1, arrEnd)
            result[entitlementKey] = if (arrContent.isBlank()) emptyList() else splitJsonArrayValues(arrContent)

            pos = arrEnd + 1
        }
        return result
    }

    private fun findMatchingBracket(json: String, openPos: Int, openChar: Char, closeChar: Char): Int {
        var depth = 0
        var inString = false
        var i = openPos
        while (i < json.length) {
            val c = json[i]
            if (c == '"' && (i == 0 || json[i - 1] != '\\')) {
                inString = !inString
            } else if (!inString) {
                if (c == openChar) depth++
                else if (c == closeChar) {
                    depth--
                    if (depth == 0) return i
                }
            }
            i++
        }
        return -1
    }

    private fun splitJsonArrayValues(content: String): List<String> {
        return content.split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
            .map { unescapeJson(it) }
    }

    private fun parseKeyValuePairs(content: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val pairs = content.split(",")
        for (pair in pairs) {
            val colonIdx = pair.indexOf(':')
            if (colonIdx == -1) continue
            val k = pair.substring(0, colonIdx).trim().removeSurrounding("\"")
            val v = pair.substring(colonIdx + 1).trim().removeSurrounding("\"")
            if (k.isNotBlank()) result[unescapeJson(k)] = unescapeJson(v)
        }
        return result
    }

    private fun escapeJson(value: String): String =
        value.replace("\\", "\\\\").replace("\"", "\\\"")

    private fun unescapeJson(value: String): String =
        value.replace("\\\"", "\"").replace("\\\\", "\\")

    companion object {
        private const val CACHE_KEY = "payment_config_cache"
    }
}
