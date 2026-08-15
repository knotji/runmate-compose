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
import kotlinx.datetime.TimeZone
import kotlin.time.Instant

sealed interface HealthDashboardUiState {
    data class Loading(
        val previous: HealthDashboardData? = null,
        val previousBodyPicture: BodyPictureModel? = null,
    ) : HealthDashboardUiState
    data object Unavailable : HealthDashboardUiState
    data class PermissionRequired(val missing: Set<String>) : HealthDashboardUiState
    data class Content(val data: HealthDashboardData, val bodyPicture: BodyPictureModel) : HealthDashboardUiState
    data class Error(
        val message: String,
        val previous: HealthDashboardData? = null,
        val previousBodyPicture: BodyPictureModel? = null,
    ) : HealthDashboardUiState
}

class HealthDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HealthDashboardRepository(application)
    private val mutableState = MutableStateFlow<HealthDashboardUiState>(HealthDashboardUiState.Loading())
    val state: StateFlow<HealthDashboardUiState> = mutableState.asStateFlow()
    private var refreshJob: Job? = null
    private val bodyPicturePolicy: BodyPicturePolicy = InitialBodyPicturePolicy()

    fun refresh() {
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            val previous = when (val current = mutableState.value) {
                is HealthDashboardUiState.Content -> current.data
                is HealthDashboardUiState.Loading -> current.previous
                is HealthDashboardUiState.Error -> current.previous
                else -> null
            }
            val previousBodyPicture = when (val current = mutableState.value) {
                is HealthDashboardUiState.Content -> current.bodyPicture
                is HealthDashboardUiState.Loading -> current.previousBodyPicture
                is HealthDashboardUiState.Error -> current.previousBodyPicture
                else -> null
            }
            mutableState.value = HealthDashboardUiState.Loading(previous, previousBodyPicture)
            mutableState.value = try {
                when (val result = PerformanceMonitor.measure("health_dashboard_load") { repository.load() }) {
                    HealthLoadResult.Unavailable -> HealthDashboardUiState.Unavailable
                    is HealthLoadResult.PermissionRequired -> HealthDashboardUiState.PermissionRequired(result.missing)
                    is HealthLoadResult.Success -> HealthDashboardUiState.Content(
                        data = result.data,
                        bodyPicture = bodyPicturePolicy.select(
                            BodyPictureInput(
                                facts = result.data.facts.withCalculatedRecovery(result.data),
                                trend = result.data.sevenDayTrend,
                                effectiveAt = Instant.fromEpochMilliseconds(result.data.syncedAt.toEpochMilli()),
                                timeZone = TimeZone.currentSystemDefault(),
                            ),
                        ),
                    )
                }
            } catch (error: Exception) {
                HealthDashboardUiState.Error(error.message ?: "Health data could not be read", previous, previousBodyPicture)
            }
        }
    }

    private fun List<HealthFact>.withCalculatedRecovery(data: HealthDashboardData): List<HealthFact> {
        val sleep = filterIsInstance<SleepFact>().maxByOrNull(HealthFact::observedAt)
        val hrv = filterIsInstance<HrvFact>().maxByOrNull(HealthFact::observedAt)
        val restingHeartRate = filterIsInstance<RestingHeartRateFact>().maxByOrNull(HealthFact::observedAt)
        val sleepingHeartRate = filterIsInstance<SleepingHeartRateFact>().maxByOrNull(HealthFact::observedAt)
        val result = WholeMateBodyScoreCalculator.recovery(
            RecoveryEvidence(
                sleepHours = sleep?.duration?.inWholeMinutes?.div(60.0),
                sleepBaselineHours = data.sevenDayTrend.dropLast(1).mapNotNull(DailyHealthPoint::sleepHours),
                hrvRmssdMillis = hrv?.rmssdMillis,
                hrvBaselineMillis = data.sevenDayTrend.dropLast(1).mapNotNull(DailyHealthPoint::hrvRmssdMillis),
                restingHeartRateBpm = restingHeartRate?.beatsPerMinute?.toDouble(),
                restingHeartRateBaselineBpm = data.sevenDayTrend.dropLast(1).mapNotNull(DailyHealthPoint::restingHeartRateBpm),
                sleepingHeartRateBpm = sleepingHeartRate?.beatsPerMinute,
                sleepingHeartRateBaselineBpm = data.sevenDayTrend.dropLast(1).mapNotNull(DailyHealthPoint::sleepingHeartRateBpm),
            ),
        )
        return when (result) {
            is ScoreCalculation.Available -> this + RecoveryScoreFact(
                score = result.value,
                observedAt = data.facts.maxOfOrNull(HealthFact::observedAt) ?: Instant.fromEpochMilliseconds(data.syncedAt.toEpochMilli()),
                source = HealthSource(result.modelVersion),
                freshness = Freshness.FRESH,
            )
            is ScoreCalculation.InsufficientData -> this
        }
    }
}
