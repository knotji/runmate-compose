package com.runmate.compose.health

import kotlin.math.roundToInt

sealed interface ScoreCalculation<out T> {
    data class Available<T>(val value: T, val modelVersion: String) : ScoreCalculation<T>
    data class InsufficientData(val missing: Set<String>) : ScoreCalculation<Nothing>
}

data class RecoveryEvidence(
    val sleepHours: Double?,
    val sleepBaselineHours: List<Double>,
    val hrvRmssdMillis: Double?,
    val hrvBaselineMillis: List<Double>,
    val restingHeartRateBpm: Double?,
    val restingHeartRateBaselineBpm: List<Double>,
    val sleepingHeartRateBpm: Double? = null,
    val sleepingHeartRateBaselineBpm: List<Double> = emptyList(),
)

object WholeMateBodyScoreCalculator {
    const val RECOVERY_MODEL_VERSION = "wholemate-recovery-v1-experimental"
    const val MINIMUM_BASELINE_DAYS = 6

    fun recovery(evidence: RecoveryEvidence): ScoreCalculation<Int> {
        val missing = buildSet {
            if (!evidence.sleepHours.isPositiveFinite()) add("sleep_today")
            if (!evidence.sleepBaselineHours.hasBaseline()) add("sleep_baseline_6d")
            val hrvReady = evidence.hrvRmssdMillis.isPositiveFinite() && evidence.hrvBaselineMillis.hasBaseline()
            val restingHeartRateReady = evidence.restingHeartRateBpm.isPositiveFinite() && evidence.restingHeartRateBaselineBpm.hasBaseline()
            val sleepingHeartRateReady = evidence.sleepingHeartRateBpm.isPositiveFinite() && evidence.sleepingHeartRateBaselineBpm.hasBaseline()
            if (!hrvReady && !restingHeartRateReady && !sleepingHeartRateReady) add("autonomic_signal_and_baseline")
        }
        if (missing.isNotEmpty()) return ScoreCalculation.InsufficientData(missing)

        val sleep = higherIsBetter(evidence.sleepHours!!, evidence.sleepBaselineHours.average(), tolerance = 0.25)
        val components = buildList {
            add(sleep to 0.35)
            if (evidence.hrvRmssdMillis.isPositiveFinite() && evidence.hrvBaselineMillis.hasBaseline()) {
                add(higherIsBetter(evidence.hrvRmssdMillis!!, evidence.hrvBaselineMillis.average(), 0.20) to 0.40)
            }
            if (evidence.restingHeartRateBpm.isPositiveFinite() && evidence.restingHeartRateBaselineBpm.hasBaseline()) {
                add(lowerIsBetter(evidence.restingHeartRateBpm!!, evidence.restingHeartRateBaselineBpm.average(), 0.10) to 0.25)
            } else if (evidence.sleepingHeartRateBpm.isPositiveFinite() && evidence.sleepingHeartRateBaselineBpm.hasBaseline()) {
                add(lowerIsBetter(evidence.sleepingHeartRateBpm!!, evidence.sleepingHeartRateBaselineBpm.average(), 0.10) to 0.25)
            }
        }
        val totalWeight = components.sumOf { it.second }
        val score = (components.sumOf { (value, weight) -> value * weight } / totalWeight).roundToInt().coerceIn(0, 100)
        return ScoreCalculation.Available(score, RECOVERY_MODEL_VERSION)
    }

    private fun higherIsBetter(current: Double, baseline: Double, tolerance: Double): Double =
        (50.0 + 50.0 * ((current / baseline - 1.0) / tolerance)).coerceIn(0.0, 100.0)

    private fun lowerIsBetter(current: Double, baseline: Double, tolerance: Double): Double =
        (50.0 - 50.0 * ((current / baseline - 1.0) / tolerance)).coerceIn(0.0, 100.0)

    private fun Double?.isPositiveFinite(): Boolean = this != null && isFinite() && this > 0.0

    private fun List<Double>.hasBaseline(): Boolean =
        size >= MINIMUM_BASELINE_DAYS && all { it.isFinite() && it > 0.0 }
}
