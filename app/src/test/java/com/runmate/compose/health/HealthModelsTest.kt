package com.runmate.compose.health

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class HealthModelsTest {
    private val bangkok = ZoneId.of("Asia/Bangkok")

    @Test fun sleepFormattingUsesTypedDurationAndRequestedZone() {
        val start = Instant.parse("2026-08-14T16:30:00Z")
        val value = SleepSummary(Duration.ofMinutes(455), start, start.plusSeconds(455 * 60L))

        assertEquals("7h 35m • ended 15 Aug, 07:05", HealthDisplayFormatter.sleep(value, bangkok))
    }

    @Test fun heartRateFormattingUsesTypedMeasurement() {
        val value = HeartRateSummary(61, Instant.parse("2026-08-15T00:15:00Z"))

        assertEquals("61 bpm • 15 Aug, 07:15", HealthDisplayFormatter.heartRate(value, bangkok))
    }

    @Test fun missingValuesRemainExplicit() {
        assertEquals("No sleep data in the last 30 days", HealthDisplayFormatter.sleep(null, bangkok))
        assertEquals("No heart-rate data in the last 30 days", HealthDisplayFormatter.heartRate(null, bangkok))
    }

    @Test fun invalidHeartRateIsRejected() {
        assertThrows(IllegalArgumentException::class.java) {
            HeartRateSummary(0, Instant.parse("2026-08-15T00:15:00Z"))
        }
    }
}
