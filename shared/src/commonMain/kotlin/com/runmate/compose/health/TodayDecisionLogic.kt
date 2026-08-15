package com.runmate.compose.health

import kotlin.math.abs

enum class ShapingSignal { SLEEPING_HEART_RATE, SLEEP_DURATION }
enum class BaselineDirection { ABOVE, BELOW, CLOSE }
enum class ShapingStrength { STRONG, NEUTRAL }

data class ShapingFact(
    val signal: ShapingSignal,
    val current: Double,
    val baseline: Double,
    val deviationPercent: Double,
    val direction: BaselineDirection,
    val strength: ShapingStrength,
    val freshness: Freshness,
    val evidenceClass: EvidenceClass,
    val qualitySampleCount: Int,
)

object ShapingFactRanker {
    private const val MINIMUM_BASELINE_DAYS = 6

    fun rank(facts: List<HealthFact>, trend: List<DailyHealthPoint>): List<ShapingFact> {
        val prior = trend.dropLast(1)
        val candidates = buildList {
            val sleepingHeartRate = facts.filterIsInstance<SleepingHeartRateFact>().maxByOrNull(HealthFact::observedAt)
            val sleepingHeartRateBaseline = prior.mapNotNull(DailyHealthPoint::sleepingHeartRateBpm)
            if (sleepingHeartRate != null && sleepingHeartRateBaseline.size >= MINIMUM_BASELINE_DAYS) {
                add(
                    fact(
                        signal = ShapingSignal.SLEEPING_HEART_RATE,
                        current = sleepingHeartRate.beatsPerMinute,
                        baseline = sleepingHeartRateBaseline.average(),
                        neutralBandPercent = 2.0,
                        freshness = sleepingHeartRate.freshness,
                        evidenceClass = sleepingHeartRate.evidenceClass,
                        qualitySampleCount = sleepingHeartRate.sampleCount,
                    ),
                )
            }

            val sleep = facts.filterIsInstance<SleepFact>().maxByOrNull(HealthFact::observedAt)
            val sleepBaseline = prior.mapNotNull(DailyHealthPoint::sleepHours)
            if (sleep != null && sleepBaseline.size >= MINIMUM_BASELINE_DAYS) {
                add(
                    fact(
                        signal = ShapingSignal.SLEEP_DURATION,
                        current = sleep.duration.inWholeMinutes / 60.0,
                        baseline = sleepBaseline.average(),
                        neutralBandPercent = 5.0,
                        freshness = sleep.freshness,
                        evidenceClass = sleep.evidenceClass,
                        qualitySampleCount = sleepBaseline.size,
                    ),
                )
            }
        }

        return candidates.sortedWith(
            compareByDescending<ShapingFact> { it.freshness == Freshness.FRESH }
                .thenByDescending { it.qualitySampleCount }
                .thenByDescending { it.strength == ShapingStrength.STRONG }
                .thenByDescending { abs(it.deviationPercent) }
                .thenBy { it.signal.ordinal },
        ).take(2)
    }

    private fun fact(
        signal: ShapingSignal,
        current: Double,
        baseline: Double,
        neutralBandPercent: Double,
        freshness: Freshness,
        evidenceClass: EvidenceClass,
        qualitySampleCount: Int,
    ): ShapingFact {
        val deviation = (current / baseline - 1.0) * 100.0
        val close = abs(deviation) <= neutralBandPercent
        return ShapingFact(
            signal = signal,
            current = current,
            baseline = baseline,
            deviationPercent = deviation,
            direction = when {
                close -> BaselineDirection.CLOSE
                deviation > 0.0 -> BaselineDirection.ABOVE
                else -> BaselineDirection.BELOW
            },
            strength = if (close) ShapingStrength.NEUTRAL else ShapingStrength.STRONG,
            freshness = freshness,
            evidenceClass = evidenceClass,
            qualitySampleCount = qualitySampleCount,
        )
    }
}

enum class NextAction { REVIEW_DETAILS, COMPLETE_HEALTH_ACCESS, REFRESH_TODAY, KEEP_USUAL_PLAN }

object NextActionPolicy {
    fun select(
        shapingFacts: List<ShapingFact>,
        bodyPicture: BodyPictureModel?,
        healthAccessIncomplete: Boolean = false,
    ): NextAction = when {
        healthAccessIncomplete -> NextAction.COMPLETE_HEALTH_ACCESS
        shapingFacts.any { it.freshness == Freshness.STALE } ||
            bodyPicture?.signals.orEmpty().any { it.state == SignalAvailability.STALE } -> NextAction.REFRESH_TODAY
        shapingFacts.any { it.strength == ShapingStrength.STRONG } -> NextAction.REVIEW_DETAILS
        else -> NextAction.KEEP_USUAL_PLAN
    }
}
