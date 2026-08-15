package com.runmate.compose.health

import kotlin.time.Duration
import kotlin.time.Instant

enum class EvidenceClass {
    MEASURED, CALCULATED, USER_REPORTED, OBSERVED_PATTERN, AI_INTERPRETATION, MISSING,
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
    val evidenceClass: EvidenceClass get() = EvidenceClass.MEASURED
}

data class RecoveryScoreFact(
    val score: Int,
    override val observedAt: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    override val evidenceClass: EvidenceClass = EvidenceClass.CALCULATED
    init { require(score in 0..100) { "Recovery score must be 0..100" } }
}

data class StrainScoreFact(
    val score: Double,
    override val observedAt: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    override val evidenceClass: EvidenceClass = EvidenceClass.CALCULATED
    init { require(score >= 0.0 && score.isFinite()) { "Strain score must be finite and non-negative" } }
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
        require(!duration.isNegative()) { "Sleep duration cannot be negative" }
        require(end >= start) { "Sleep end must not precede start" }
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

data class RestingHeartRateFact(
    val beatsPerMinute: Long,
    override val observedAt: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    init { require(beatsPerMinute > 0) { "Resting heart rate must be positive" } }
}

data class SleepingHeartRateFact(
    val beatsPerMinute: Double,
    val sampleCount: Int,
    override val observedAt: Instant,
    override val source: HealthSource,
    override val freshness: Freshness,
) : HealthFact {
    override val evidenceClass: EvidenceClass = EvidenceClass.CALCULATED
    init {
        require(beatsPerMinute > 0.0 && beatsPerMinute.isFinite()) { "Sleeping heart rate must be positive and finite" }
        require(sampleCount > 0) { "Sleeping heart rate requires samples" }
    }
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
        require(!duration.isNegative()) { "Activity duration cannot be negative" }
        require(end >= start) { "Activity end must not precede start" }
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
        require(end >= start) { "Steps end must not precede start" }
    }
}

fun assessFreshness(observedAt: Instant, assessedAt: Instant, maximumAge: Duration): Freshness =
    if (observedAt < assessedAt - maximumAge) Freshness.STALE else Freshness.FRESH
