package com.marketfinder.core.presentation.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.payment.SubscriptionStatus

/**
 * Subscription Badge - Pre-Built Template Component
 *
 * Active/expired/trial status badge for subscription display.
 *
 * Usage:
 * ```
 * SubscriptionBadge(status = subscriptionStatus)
 * ```
 */
@Composable
fun SubscriptionBadge(
    status: SubscriptionStatus,
    modifier: Modifier = Modifier
) {
    val (label, containerColor, contentColor) = when (status) {
        is SubscriptionStatus.Active -> Triple(
            "Active",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        is SubscriptionStatus.Trial -> Triple(
            "Trial",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        is SubscriptionStatus.Expired -> Triple(
            "Expired",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        is SubscriptionStatus.None -> Triple(
            "Free",
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = containerColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor
        )
    }
}
