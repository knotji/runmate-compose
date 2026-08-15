package com.runmate.compose.health

import java.time.Duration
import java.time.Instant

enum class EvidenceClass {
    MEASURED,
    CALCULATED,
    USER_REPORTED,
    OBSERVED_PATTERN,
    AI_INTERPRETATION,
    MISSING,
}

enum class Freshness { FRESH, STALE }

data class HealthSource(
    val provider: String,
    val originId: String? = null,
    val originLabel: String? = null,
)

sealed interface HealthFact {
    val observedAt: Instant
    val source: HealthSource
    val freshness: Freshness
    val evidenceClass: EvidenceClass
        get() = EvidenceClass.MEASURED
}

data class SleepFact(
    val duration: Duration,
    val start: Instant,
    val end: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    override val observedAt: Instant = end
    init {
        require(!duration.isNegative) { "Sleep duration cannot be negative" }
        require(!end.isBefore(start)) { "Sleep end must not precede start" }
    }
}

data class HeartRateFact(
    val beatsPerMinute: Long,
    override val observedAt: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    init { require(beatsPerMinute > 0) { "Heart rate must be positive" } }
}

data class HrvFact(
    val rmssdMillis: Double,
    override val observedAt: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    init { require(rmssdMillis > 0.0 && rmssdMillis.isFinite()) { "HRV RMSSD must be positive and finite" } }
}

data class RespiratoryRateFact(
    val breathsPerMinute: Double,
    override val observedAt: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    init { require(breathsPerMinute > 0.0 && breathsPerMinute.isFinite()) { "Respiratory rate must be positive and finite" } }
}

data class ActivityFact(
    val typeCode: Int,
    val title: String?,
    val duration: Duration,
    val start: Instant,
    val end: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    override val observedAt: Instant = end
    init {
        require(!duration.isNegative) { "Activity duration cannot be negative" }
        require(!end.isBefore(start)) { "Activity end must not precede start" }
    }
}

data class StepsFact(
    val count: Long,
    val start: Instant,
    val end: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    override val observedAt: Instant = end
    init {
        require(count >= 0) { "Step count cannot be negative" }
        require(!end.isBefore(start)) { "Steps end must not precede start" }
    }
}

internal fun freshness(observedAt: Instant, assessedAt: Instant, maximumAge: Duration): Freshness =
    if (observedAt.isBefore(assessedAt.minus(maximumAge))) Freshness.STALE else Freshness.FRESH
