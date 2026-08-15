package com.runmate.compose.health

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class SignalOrigin(
    val packageName: String,
    val appLabel: String,
)

data class SleepSummary(
    val duration: Duration,
    val startedAt: Instant,
    val endedAt: Instant,
    val origin: SignalOrigin? = null,
) {
    init {
        require(!duration.isNegative) { "Sleep duration cannot be negative" }
        require(!endedAt.isBefore(startedAt)) { "Sleep end must not precede start" }
    }
}

data class HeartRateSummary(
    val beatsPerMinute: Long,
    val measuredAt: Instant,
    val origin: SignalOrigin? = null,
) {
    init {
        require(beatsPerMinute > 0) { "Heart rate must be positive" }
    }
}

data class RestingHeartRateSummary(
    val beatsPerMinute: Long,
    val measuredAt: Instant,
    val origin: SignalOrigin? = null,
) {
    init { require(beatsPerMinute > 0) { "Resting heart rate must be positive" } }
}

data class SleepingHeartRateSummary(
    val beatsPerMinute: Double,
    val sampleCount: Int,
    val measuredUntil: Instant,
) {
    init {
        require(beatsPerMinute > 0.0 && beatsPerMinute.isFinite()) { "Sleeping heart rate must be positive and finite" }
        require(sampleCount > 0) { "Sleeping heart rate requires samples" }
    }
}

data class HrvSummary(
    val rmssdMillis: Double,
    val measuredAt: Instant,
    val origin: SignalOrigin? = null,
) {
    init {
        require(rmssdMillis > 0.0 && rmssdMillis.isFinite()) { "HRV RMSSD must be a positive finite value" }
    }
}

data class RespiratoryRateSummary(
    val breathsPerMinute: Double,
    val measuredAt: Instant,
    val origin: SignalOrigin? = null,
) {
    init {
        require(breathsPerMinute > 0.0 && breathsPerMinute.isFinite()) { "Respiratory rate must be a positive finite value" }
    }
}

data class ActivitySummary(
    val typeCode: Int,
    val title: String?,
    val duration: Duration,
    val startedAt: Instant,
    val endedAt: Instant,
    val origin: SignalOrigin? = null,
) {
    init {
        require(!duration.isNegative) { "Activity duration cannot be negative" }
        require(!endedAt.isBefore(startedAt)) { "Activity end must not precede start" }
    }
}

data class DailyStepsSummary(
    val count: Long,
    val startedAt: Instant,
    val endedAt: Instant,
) {
    init {
        require(count >= 0) { "Step count cannot be negative" }
        require(!endedAt.isBefore(startedAt)) { "Steps interval end must not precede start" }
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

    fun hrv(value: HrvSummary?, zoneId: ZoneId = ZoneId.systemDefault()): String =
        value?.let { "%.1f ms • %s".format(it.rmssdMillis, time(it.measuredAt, zoneId)) }
            ?: "No HRV data in the last 30 days"

    fun respiratoryRate(value: RespiratoryRateSummary?, zoneId: ZoneId = ZoneId.systemDefault()): String =
        value?.let { "%.1f breaths/min • %s".format(it.breathsPerMinute, time(it.measuredAt, zoneId)) }
            ?: "No respiratory-rate data in the last 30 days"

    fun activity(value: ActivitySummary?, zoneId: ZoneId = ZoneId.systemDefault()): String {
        if (value == null) return "No activity data in the last 30 days"
        val name = value.title?.takeIf(String::isNotBlank) ?: "Activity type ${value.typeCode}"
        return "$name • ${value.duration.toMinutes()}m • ${time(value.endedAt, zoneId)}"
    }

    fun steps(value: DailyStepsSummary?): String =
        value?.let { "%,d steps today".format(it.count) } ?: "No step data for today"

    fun time(value: Instant, zoneId: ZoneId = ZoneId.systemDefault()): String =
        DateTimeFormatter.ofPattern("d MMM, HH:mm").withZone(zoneId).format(value)
}
