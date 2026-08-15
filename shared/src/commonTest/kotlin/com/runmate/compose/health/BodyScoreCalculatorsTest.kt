package com.runmate.compose.health

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class BodyScoreCalculatorsTest {
    @Test fun recoveryAtPersonalBaselineIsNeutral() {
        val result = WholeMateBodyScoreCalculator.recovery(
            recoveryEvidence(sleep = 7.0, hrv = 50.0, restingHeartRate = 60.0),
        )
        val available = assertIs<ScoreCalculation.Available<Int>>(result)
        assertEquals(50, available.value)
        assertEquals(WholeMateBodyScoreCalculator.RECOVERY_MODEL_VERSION, available.modelVersion)
    }

    @Test fun recoveryRespondsInExpectedDirectionsAndStaysBounded() {
        val high = assertIs<ScoreCalculation.Available<Int>>(
            WholeMateBodyScoreCalculator.recovery(recoveryEvidence(9.0, 65.0, 50.0)),
        )
        val low = assertIs<ScoreCalculation.Available<Int>>(
            WholeMateBodyScoreCalculator.recovery(recoveryEvidence(4.0, 30.0, 75.0)),
        )
        assertEquals(100, high.value)
        assertEquals(0, low.value)
    }

    @Test fun recoveryRequiresSleepAndAtLeastOneAutonomicSignalWithBaselines() {
        val result = WholeMateBodyScoreCalculator.recovery(
            recoveryEvidence(7.0, hrv = null, restingHeartRate = null).copy(sleepBaselineHours = List(5) { 7.0 }),
        )
        val insufficient = assertIs<ScoreCalculation.InsufficientData>(result)
        assertTrue("autonomic_signal_and_baseline" in insufficient.missing)
        assertTrue("sleep_baseline_6d" in insufficient.missing)
    }

    @Test fun recoveryCanUseSleepAndRestingHeartRateWhenHrvIsMissing() {
        val result = WholeMateBodyScoreCalculator.recovery(recoveryEvidence(7.0, hrv = null, restingHeartRate = 60.0))
        assertEquals(50, assertIs<ScoreCalculation.Available<Int>>(result).value)
    }

    @Test fun recoveryCanUseDerivedSleepingHeartRateWhenMeasuredAutonomicRecordsAreMissing() {
        val result = WholeMateBodyScoreCalculator.recovery(
            recoveryEvidence(7.0, hrv = null, restingHeartRate = null).copy(
                sleepingHeartRateBpm = 52.0,
                sleepingHeartRateBaselineBpm = List(6) { 52.0 },
            ),
        )
        assertEquals(50, assertIs<ScoreCalculation.Available<Int>>(result).value)
    }

    @Test fun samsungDeviceFixtureProducesAuditableRecoveryEstimate() {
        val result = WholeMateBodyScoreCalculator.recovery(
            RecoveryEvidence(
                sleepHours = 403.0 / 60.0,
                sleepBaselineHours = listOf(6.68, 6.72, 6.60, 6.75, 6.64, 6.63),
                hrvRmssdMillis = null,
                hrvBaselineMillis = emptyList(),
                restingHeartRateBpm = null,
                restingHeartRateBaselineBpm = emptyList(),
                sleepingHeartRateBpm = 53.2691358,
                sleepingHeartRateBaselineBpm = listOf(
                    52.7765727, 52.9554566, 47.4776119,
                    56.4230769, 47.6, 49.4333333,
                ),
            ),
        )

        val available = assertIs<ScoreCalculation.Available<Int>>(result)
        assertEquals(42, available.value)
        assertEquals("wholemate-recovery-v1-experimental", available.modelVersion)
    }

    @Test fun elevatedSleepingHeartRateLowersEstimateWhileSleepStaysConstant() {
        fun estimate(sleepingHeartRate: Double) = assertIs<ScoreCalculation.Available<Int>>(
            WholeMateBodyScoreCalculator.recovery(
                recoveryEvidence(7.0, hrv = null, restingHeartRate = null).copy(
                    sleepingHeartRateBpm = sleepingHeartRate,
                    sleepingHeartRateBaselineBpm = List(6) { 50.0 },
                ),
            ),
        ).value

        assertTrue(estimate(52.0) < estimate(50.0))
        assertTrue(estimate(50.0) < estimate(48.0))
    }

    private fun recoveryEvidence(
        sleep: Double?,
        hrv: Double?,
        restingHeartRate: Double?,
    ) = RecoveryEvidence(
        sleepHours = sleep,
        sleepBaselineHours = List(7) { 7.0 },
        hrvRmssdMillis = hrv,
        hrvBaselineMillis = List(7) { 50.0 },
        restingHeartRateBpm = restingHeartRate,
        restingHeartRateBaselineBpm = List(7) { 60.0 },
    )
}
