package com.runmate.compose.state

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow

enum class AppDestination { TODAY, HEALTH, MOVE, YOU }

class RunMateAppStore(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    val destination: StateFlow<AppDestination> = savedStateHandle.getStateFlow(DESTINATION_KEY, AppDestination.TODAY)

    fun navigate(destination: AppDestination) {
        savedStateHandle[DESTINATION_KEY] = destination
    }

    private companion object { const val DESTINATION_KEY = "app_destination" }
}
