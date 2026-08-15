package com.runmate.compose.health

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class SleepAggregationTest {
    private val bangkok = ZoneId.of("Asia/Bangkok")
    private val wakeDate = LocalDate.of(2026, 8, 15)

    @Test
    fun joinsSamsungContinuationWithoutCountingAwakeGap() {
        val result = aggregate(
            interval("2026-08-14T16:15:00Z", "2026-08-14T21:05:00Z", "samsung"),
            interval("2026-08-14T21:12:00Z", "2026-08-14T23:05:00Z", "samsung"),
        )
        assertEquals(Duration.ofMinutes(403), result.duration)
        assertEquals(Instant.parse("2026-08-14T16:15:00Z"), result.startedAt)
        assertEquals(Instant.parse("2026-08-14T23:05:00Z"), result.endedAt)
    }

    @Test
    fun overlappingRecordsFromSameSourceAreNotDoubleCounted() {
        val result = aggregate(
            interval("2026-08-14T16:00:00Z", "2026-08-14T22:00:00Z", "samsung"),
            interval("2026-08-14T20:00:00Z", "2026-08-14T23:00:00Z", "samsung"),
        )
        assertEquals(Duration.ofHours(7), result.duration)
    }

    @Test
    fun duplicateProviderDoesNotInflateDuration() {
        val result = aggregate(
            interval("2026-08-14T16:00:00Z", "2026-08-14T23:00:00Z", "samsung"),
            interval("2026-08-14T16:00:00Z", "2026-08-14T20:00:00Z", "google_fit"),
        )
        assertEquals(Duration.ofHours(7), result.duration)
        assertEquals("samsung", result.sourceId)
    }

    @Test
    fun longestClusterWinsOverLaterNap() {
        val result = aggregate(
            interval("2026-08-14T16:00:00Z", "2026-08-14T22:00:00Z", "samsung"),
            interval("2026-08-15T05:00:00Z", "2026-08-15T06:00:00Z", "samsung"),
        )
        assertEquals(Duration.ofHours(6), result.duration)
        assertEquals(Instant.parse("2026-08-14T22:00:00Z"), result.endedAt)
    }

    @Test
    fun gapBeyondContinuationThresholdStartsANewCluster() {
        val result = aggregate(
            interval("2026-08-14T16:00:00Z", "2026-08-14T20:00:00Z", "samsung"),
            interval("2026-08-14T22:00:00Z", "2026-08-14T23:00:00Z", "samsung"),
        )
        assertEquals(Duration.ofHours(4), result.duration)
    }

    private fun aggregate(vararg intervals: SleepInterval): AggregatedSleep =
        requireNotNull(SleepAggregation.longestForWakeDate(intervals.toList(), wakeDate, bangkok))

    private fun interval(start: String, end: String, source: String) =
        SleepInterval(Instant.parse(start), Instant.parse(end), source)
}
