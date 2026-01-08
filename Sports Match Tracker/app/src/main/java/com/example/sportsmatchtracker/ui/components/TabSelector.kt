package com.example.sportsmatchtracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun <T> TabSelector(
    tabs: List<TabItem<T?>>,
    selectedTab: T,
    onTabSelected: (T?) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryScrollableTabRow(
        selectedTabIndex = tabs.indexOfFirst { it.value == selectedTab }.coerceAtLeast(0),
        modifier = modifier.fillMaxWidth(),
        edgePadding = 16.dp
    ) {
        tabs.forEach { tab ->
            Tab(
                selected = selectedTab == tab.value,
                onClick = { onTabSelected(tab.value) },
                text = { 
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            )
        }
    }
}

data class TabItem<T>(
    val value: T,
    val label: String
)
