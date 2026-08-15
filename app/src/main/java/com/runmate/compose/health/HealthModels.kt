package com.runmate.compose.health

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class SleepSummary(
    val duration: Duration,
    val startedAt: Instant,
    val endedAt: Instant,
) {
    init {
        require(!duration.isNegative) { "Sleep duration cannot be negative" }
        require(!endedAt.isBefore(startedAt)) { "Sleep end must not precede start" }
    }
}

data class HeartRateSummary(
    val beatsPerMinute: Long,
    val measuredAt: Instant,
) {
    init {
        require(beatsPerMinute > 0) { "Heart rate must be positive" }
    }
}

object HealthDisplayFormatter {
    fun sleep(value: SleepSummary?, zoneId: ZoneId = ZoneId.systemDefault()): String {
        if (value == null) return "No sleep data in the last 30 days"
        val minutes = value.duration.toMinutes()
        return "${minutes / 60}h ${minutes % 60}m • ended ${time(value.endedAt, zoneId)}"
    }

    fun heartRate(value: HeartRateSummary?, zoneId: ZoneId = ZoneId.systemDefault()): String =
        value?.let { "${it.beatsPerMinute} bpm • ${time(it.measuredAt, zoneId)}" }
            ?: "No heart-rate data in the last 30 days"

    fun time(value: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
        DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(zoneId).format(value)
}
