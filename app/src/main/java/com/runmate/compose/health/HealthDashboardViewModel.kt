package com.runmate.compose.health

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runmate.compose.core.performance.PerformanceMonitor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HealthDashboardUiState {
    data object Loading : HealthDashboardUiState
    data object Unavailable : HealthDashboardUiState
    data class PermissionRequired(val missing: Set<String>) : HealthDashboardUiState
    data class Content(val data: HealthDashboardData) : HealthDashboardUiState
    data class Error(val message: String) : HealthDashboardUiState
}

class HealthDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HealthDashboardRepository(application)
    private val mutableState = MutableStateFlow<HealthDashboardUiState>(HealthDashboardUiState.Loading)
    val state: StateFlow<HealthDashboardUiState> = mutableState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            mutableState.value = HealthDashboardUiState.Loading
            mutableState.value = try {
                when (val result = PerformanceMonitor.measure("health_dashboard_load") { repository.load() }) {
                    HealthLoadResult.Unavailable -> HealthDashboardUiState.Unavailable
                    is HealthLoadResult.PermissionRequired -> HealthDashboardUiState.PermissionRequired(result.missing)
                    is HealthLoadResult.Success -> HealthDashboardUiState.Content(result.data)
                }
            } catch (error: Exception) {
                HealthDashboardUiState.Error(error.message ?: "Health data could not be read")
            }
        }
    }
}
