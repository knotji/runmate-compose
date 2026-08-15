package com.runmate.compose.core.state

sealed interface LoadState<out T> {
    data object Idle : LoadState<Nothing>
    data class Loading<T>(val previous: T? = null) : LoadState<T>
    data class Ready<T>(val value: T, val receivedAtEpochMillis: Long) : LoadState<T>
    data class Empty(val reason: String) : LoadState<Nothing>
    data class Failed<T>(val message: String, val previous: T? = null, val retryable: Boolean = true) : LoadState<T>
}
fun <T> LoadState<T>.visibleValue(): T? = when (this) {
    is LoadState.Ready -> value
    is LoadState.Loading -> previous
    is LoadState.Failed -> previous
    LoadState.Idle, is LoadState.Empty -> null
}
