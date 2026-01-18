package com.example.sportsmatchtracker.ui.utils

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun <T> filterList(
    list: List<T>,
    query: String,
    selector: (T) -> String
): List<T> {
    if (query.isBlank()) return list
    return list.filter { selector(it).contains(query, ignoreCase = true) }
}

fun <T> liveSearch(
    list: List<T>,
    query: String,
    selector: (T) -> String,
    onResult: (List<T>) -> Unit
) {
    val results = if (query.isBlank()) list else list.filter {
        selector(it).contains(query, ignoreCase = true)
    }
    onResult(results)
}
