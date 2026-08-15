package com.runmate.compose.recovery

import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoverySnapshotTest {
    private val today = LocalDate.of(2026, 8, 15)

    @Test fun rejectsOutOfRangeAndStaleSnapshot() {
        val snapshot = RecoverySnapshot(
            contractVersion = 2,
            modelVersion = "whoop_style_v1",
            effectiveLocalDate = today.minusDays(1),
            calculationTimeZone = "Asia/Bangkok",
            calculatedAt = Instant.parse("2026-08-14T23:00:00Z"),
            state = RecoveryScoreState.SCORED,
            recoveryScore = 120,
            strainScore = 4.0,
            sleepScore = 80,
            energyReserve = 70,
            headline = "Preview",
            reasons = emptyList(),
            usedSignals = emptyList(),
            missingSignals = emptyList(),
        )
        val errors = snapshot.validate(today)
        assertTrue(errors.any { it.contains("not for today") })
        assertTrue(errors.any { it.contains("0..100") })
    }

    @Test fun requiresCalculationTimeZone() {
        val snapshot = RecoverySnapshot(
            contractVersion = 2,
            modelVersion = "runmate_recovery_v1",
            effectiveLocalDate = today,
            calculationTimeZone = "",
            calculatedAt = Instant.parse("2026-08-15T00:00:00Z"),
            state = RecoveryScoreState.PENDING,
            recoveryScore = null,
            strainScore = null,
            sleepScore = null,
            energyReserve = null,
            headline = "Waiting for data",
            reasons = emptyList(),
            usedSignals = emptyList(),
            missingSignals = listOf("sleep"),
        )

        assertTrue(snapshot.validate(today).any { it.contains("time zone") })
    }
}
