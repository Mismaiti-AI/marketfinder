package com.marketfinder.core.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.payment.PaymentProduct
import com.marketfinder.core.data.payment.SubscriptionStatus
import com.marketfinder.core.presentation.components.PaymentProductCard
import com.marketfinder.core.presentation.components.SubscriptionBadge

/**
 * Generic Subscription Screen - Pre-Built Template Component
 *
 * Current plan display, manage subscription, upgrade/downgrade options.
 *
 * Usage:
 * ```
 * GenericSubscriptionScreen(
 *     title = "My Subscription",
 *     currentStatus = subscriptionStatus,
 *     currentPlanName = "Pro Monthly",
 *     availablePlans = otherPlans,
 *     onChangePlan = { productId -> viewModel.changePlan(productId) },
 *     onManageSubscription = { viewModel.openManagement() },
 *     onBackClick = { navController.popBackStack() }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericSubscriptionScreen(
    title: String,
    currentStatus: SubscriptionStatus,
    modifier: Modifier = Modifier,
    currentPlanName: String? = null,
    expiresLabel: String? = null,
    availablePlans: List<PaymentProduct> = emptyList(),
    onChangePlan: ((String) -> Unit)? = null,
    onManageSubscription: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null
) {
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Current Plan
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    tonalElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Current Plan",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            SubscriptionBadge(status = currentStatus)
                        }

                        if (currentPlanName != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentPlanName,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (expiresLabel != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = expiresLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (onManageSubscription != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onManageSubscription,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Manage Subscription")
                            }
                        }
                    }
                }
            }

            // Available Plans
            if (availablePlans.isNotEmpty() && onChangePlan != null) {
                item {
                    Text(
                        text = "Available Plans",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(availablePlans) { plan ->
                    PaymentProductCard(
                        product = plan,
                        isSelected = false,
                        onClick = { onChangePlan(plan.id) }
                    )
                }
            }
        }
    }
}
