package com.marketfinder.core.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.payment.PaymentProduct
import com.marketfinder.core.presentation.components.PaymentProductCard

/**
 * Generic Paywall Screen - Pre-Built Template Component
 *
 * Product list, feature highlights, CTA button, restore purchases, terms/privacy links.
 *
 * Usage:
 * ```
 * GenericPaywallScreen(
 *     title = "Upgrade to Pro",
 *     subtitle = "Unlock all features",
 *     products = offerings,
 *     features = listOf("Unlimited access", "No ads", "Priority support"),
 *     onPurchase = { productId -> viewModel.purchase(productId) },
 *     onRestore = { viewModel.restore() },
 *     onDismiss = { navController.popBackStack() }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericPaywallScreen(
    title: String,
    products: List<PaymentProduct>,
    onPurchase: (String) -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    features: List<String> = emptyList(),
    onRestore: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    isLoading: Boolean = false,
    onTermsClick: (() -> Unit)? = null,
    onPrivacyClick: (() -> Unit)? = null
) {
    var selectedProductId by remember { mutableStateOf(products.firstOrNull()?.id) }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (onDismiss != null) {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                )
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (subtitle != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Features
            if (features.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        features.forEach { feature ->
                            Text(
                                text = "✓  $feature",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Products
            item { Spacer(modifier = Modifier.height(4.dp)) }
            items(products) { product ->
                PaymentProductCard(
                    product = product,
                    isSelected = selectedProductId == product.id,
                    onClick = { selectedProductId = product.id }
                )
            }

            // CTA
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { selectedProductId?.let { onPurchase(it) } },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = selectedProductId != null && !isLoading
                ) {
                    Text(
                        text = if (isLoading) "Processing..." else "Continue",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }

            // Restore
            if (onRestore != null) {
                item {
                    TextButton(onClick = onRestore) {
                        Text("Restore Purchases")
                    }
                }
            }

            // Terms & Privacy
            if (onTermsClick != null || onPrivacyClick != null) {
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (onTermsClick != null) {
                            TextButton(onClick = onTermsClick) {
                                Text(
                                    "Terms of Service",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        if (onPrivacyClick != null) {
                            TextButton(onClick = onPrivacyClick) {
                                Text(
                                    "Privacy Policy",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
