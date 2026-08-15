package com.runmate.compose.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runmate.compose.core.performance.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

sealed interface HealthDashboardUiState {
    data class Loading(val previous: HealthDashboardData? = null) : HealthDashboardUiState
    data object Unavailable : HealthDashboardUiState
    data class PermissionRequired(val missing: Set<String>) : HealthDashboardUiState
    data class Content(val data: HealthDashboardData) : HealthDashboardUiState
    data class Error(val message: String, val previous: HealthDashboardData? = null) : HealthDashboardUiState
}

class HealthDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HealthDashboardRepository(application)
    private val mutableState = MutableStateFlow<HealthDashboardUiState>(HealthDashboardUiState.Loading())
    val state: StateFlow<HealthDashboardUiState> = mutableState.asStateFlow()
    private var refreshJob: Job? = null

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val previous = when (val current = mutableState.value) {
                is HealthDashboardUiState.Content -> current.data
                is HealthDashboardUiState.Loading -> current.previous
                is HealthDashboardUiState.Error -> current.previous
                else -> null
            }
            mutableState.value = HealthDashboardUiState.Loading(previous)
            mutableState.value = try {
                when (val result = PerformanceMonitor.measure("health_dashboard_load") { repository.load() }) {
                    HealthLoadResult.Unavailable -> HealthDashboardUiState.Unavailable
                    is HealthLoadResult.PermissionRequired -> HealthDashboardUiState.PermissionRequired(result.missing)
                    is HealthLoadResult.Success -> HealthDashboardUiState.Content(result.data)
                }
            } catch (error: Exception) {
                HealthDashboardUiState.Error(error.message ?: "Health data could not be read", previous)
            }
        }
    }
}
