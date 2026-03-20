package com.marketfinder.core.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.marketfinder.core.data.charts.ChartDataPoint
import com.marketfinder.core.data.charts.ChartMetrics
import com.marketfinder.core.data.charts.ChartType
import com.marketfinder.core.presentation.components.BarChart
import com.marketfinder.core.presentation.components.ChartCard
import com.marketfinder.core.presentation.components.LineChart
import com.marketfinder.core.presentation.components.PieChart

/**
 * Generic Chart Screen - Pre-Built Template Component
 *
 * Screen with chart type selector tabs, data display, and refresh action.
 *
 * Usage:
 * ```
 * GenericChartScreen(
 *     title = "Analytics",
 *     chartSections = listOf(
 *         ChartSection("Revenue", ChartType.BAR, revenueData),
 *         ChartSection("Categories", ChartType.PIE, categoryData)
 *     ),
 *     onRefresh = { viewModel.refresh() },
 *     onBackClick = { navController.popBackStack() }
 * )
 * ```
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericChartScreen(
    title: String,
    chartSections: List<ChartSection>,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null,
    onRefresh: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var currentSection by remember { mutableStateOf<ChartSection?>(chartSections.first()) }


    LaunchedEffect(selectedTab) {
        currentSection = chartSections.getOrNull(selectedTab)
    }

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
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (chartSections.size > 1) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    edgePadding = 16.dp
                ) {
                    chartSections.forEachIndexed { index, section ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(section.title) }
                        )
                    }
                }
            }

            currentSection?.let {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ChartCard(
                            title = it.title,
                            subtitle = it.subtitle,
                            metrics = ChartMetrics.from(it.data)
                        ) {
                            when (it.chartType) {
                                ChartType.BAR -> BarChart(
                                    data = it.data,
                                    modifier = Modifier.fillMaxWidth().height(200.dp)
                                )

                                ChartType.LINE, ChartType.AREA -> LineChart(
                                    data = it.data,
                                    modifier = Modifier.fillMaxWidth().height(200.dp),
                                    showFill = it.chartType == ChartType.AREA
                                )

                                ChartType.PIE -> PieChart(
                                    data = it.data,
                                    isDonut = false
                                )

                                ChartType.DONUT -> PieChart(
                                    data = it.data,
                                    isDonut = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

data class ChartSection(
    val title: String,
    val chartType: ChartType,
    val data: List<ChartDataPoint>,
    val subtitle: String? = null
)
