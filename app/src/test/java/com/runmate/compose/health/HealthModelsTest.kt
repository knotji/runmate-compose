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

    @Test fun hrvAndRespiratoryFormattingPreserveUnits() {
        val instant = Instant.parse("2026-08-15T00:15:00Z")

        assertEquals("42.6 ms • 15 Aug, 07:15", HealthDisplayFormatter.hrv(HrvSummary(42.6, instant), bangkok))
        assertEquals("15.2 breaths/min • 15 Aug, 07:15", HealthDisplayFormatter.respiratoryRate(RespiratoryRateSummary(15.2, instant), bangkok))
    }

    @Test fun activityIsGenericAndDoesNotAssumeRunning() {
        val start = Instant.parse("2026-08-15T00:00:00Z")
        val activity = ActivitySummary(79, "Strength training", Duration.ofMinutes(45), start, start.plusSeconds(2700))

        assertEquals("Strength training • 45m • 15 Aug, 07:45", HealthDisplayFormatter.activity(activity, bangkok))
    }

    @Test fun untitledActivityKeepsProviderTypeCode() {
        val start = Instant.parse("2026-08-15T00:00:00Z")
        val activity = ActivitySummary(8, null, Duration.ofMinutes(30), start, start.plusSeconds(1800))

        assertEquals("Activity type 8 • 30m • 15 Aug, 07:30", HealthDisplayFormatter.activity(activity, bangkok))
    }

    @Test fun invalidPhysiologyValuesAreRejected() {
        val instant = Instant.parse("2026-08-15T00:15:00Z")
        assertThrows(IllegalArgumentException::class.java) { HrvSummary(Double.NaN, instant) }
        assertThrows(IllegalArgumentException::class.java) { RespiratoryRateSummary(0.0, instant) }
    }
}
