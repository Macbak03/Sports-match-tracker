package com.example.sportsmatchtracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.sportsmatchtracker.repository.leagues.LeaguesRepository
import com.example.sportsmatchtracker.ui.tables.view.TablesScreen
import com.example.sportsmatchtracker.ui.tables.view_model.TablesViewModel
import com.example.sportsmatchtracker.ui.theme.SportsMatchTrackerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> ItemCarousel(
    items: List<T>,
    modifier: Modifier = Modifier,
    startFromEnd: Boolean = false,
    onItemSelected: ((T) -> Unit)? = null,
    itemContent: @Composable (T) -> Unit
) {
    var currentIndex by remember(items, startFromEnd) {
        mutableStateOf(if (startFromEnd && items.isNotEmpty()) items.size - 1 else 0)
    }
    var showPicker by remember { mutableStateOf(false) }

    if (items.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No elements",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (currentIndex > 0) {
            IconButton(
                onClick = { 
                    currentIndex--
                    onItemSelected?.invoke(items[currentIndex])
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous element",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        Box(
            modifier = Modifier
                .clickable { showPicker = true }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            itemContent(items[currentIndex])
        }

        if (currentIndex < items.size - 1) {
            IconButton(
                onClick = { 
                    currentIndex++
                    onItemSelected?.invoke(items[currentIndex])
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next element",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }

    if (showPicker) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = {
                Text(text = "Choose element")
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items.forEachIndexed { index, item ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    currentIndex = index
                                    showPicker = false
                                    onItemSelected?.invoke(item)
                                }
                                .padding(vertical = 8.dp),
                            color = if (index == currentIndex) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        ) {
                            Box(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                itemContent(item)
                            }
                        }
                        if (index < items.size - 1) {
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ItemCarouselPreview() {
    SportsMatchTrackerTheme {
        ItemCarousel(
            items = listOf("Element 1", "Element 2", "Element 3"),
            startFromEnd = false,
            onItemSelected = { selectedItem ->
                println("Selected: $selectedItem")
            }
        ) { item ->
            Text(
                text = item,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
