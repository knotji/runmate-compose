package com.runmate.compose.recovery

import java.time.Instant
import java.time.LocalDate

enum class RecoveryScoreState { SCORED, PENDING, CALIBRATING, UNSCORABLE, STALE }
enum class SignalSource { MEASURED, DERIVED, SELF_REPORTED, UNAVAILABLE }

data class RecoveryReason(
    val key: String,
    val label: String,
    val detail: String,
    val source: SignalSource,
    val direction: Direction,
) {
    enum class Direction { HELPING, HURTING, NEUTRAL, UNAVAILABLE }
}
data class RecoverySnapshot(
    val contractVersion: Int,
    val modelVersion: String,
    val date: LocalDate,
    val calculatedAt: Instant,
    val state: RecoveryScoreState,
    val recoveryScore: Int?,
    val strainScore: Double?,
    val sleepScore: Int?,
    val energyReserve: Int?,
    val headline: String,
    val reasons: List<RecoveryReason>,
    val usedSignals: List<String>,
    val missingSignals: List<String>,
) {
    fun validate(today: LocalDate): List<String> = buildList {
        if (contractVersion != CURRENT_CONTRACT_VERSION) add("Unsupported recovery contract version")
        if (modelVersion.isBlank()) add("Recovery model version is required")
        if (date != today) add("Recovery snapshot is not for today")
        if (recoveryScore != null && recoveryScore !in 0..100) add("Recovery score must be 0..100")
        if (strainScore != null && strainScore !in 0.0..21.0) add("Strain score must be 0..21")
        if (sleepScore != null && sleepScore !in 0..100) add("Sleep score must be 0..100")
        if (energyReserve != null && energyReserve !in 0..100) add("Energy reserve must be 0..100")
        if (state == RecoveryScoreState.SCORED && recoveryScore == null) add("Scored recovery requires a score")
    }

    companion object { const val CURRENT_CONTRACT_VERSION = 1 }
}

sealed interface RecoverySnapshotState {
    data object NotConfigured : RecoverySnapshotState
    data object Loading : RecoverySnapshotState
    data class Available(val snapshot: RecoverySnapshot) : RecoverySnapshotState
    data class Rejected(val reasons: List<String>) : RecoverySnapshotState
    data class Failed(val message: String) : RecoverySnapshotState
}

fun interface RecoverySnapshotProvider {
    suspend fun loadToday(): RecoverySnapshotState
}

class UnconfiguredRecoverySnapshotProvider : RecoverySnapshotProvider {
    override suspend fun loadToday(): RecoverySnapshotState = RecoverySnapshotState.NotConfigured
}
