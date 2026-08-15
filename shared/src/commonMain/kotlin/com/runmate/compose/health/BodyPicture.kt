package com.runmate.compose.health

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

enum class BodyPictureSignalId { RECOVERY, STRAIN, SLEEP, RESTING_HEART_RATE, STRESS, MOVEMENT }
enum class SignalAvailability { AVAILABLE, MISSING, NOT_PERMITTED, NOT_SUPPORTED, NOT_CONNECTED, STALE, INSUFFICIENT_DATA }
enum class DataCompleteness { COMPLETE, PARTIAL, UNAVAILABLE }

data class BaselineDelta(val difference: Double, val unit: String, val sampleCount: Int)

data class BodyPictureSignal(
    val id: BodyPictureSignalId,
    val label: String,
    val value: String?,
    val unit: String?,
    val evidenceClass: EvidenceClass,
    val state: SignalAvailability,
    val baselineDelta: BaselineDelta? = null,
    val freshness: Freshness? = null,
    val source: HealthSource? = null,
    val importance: Int,
)

data class BodyPictureModel(
    val headline: String?,
    val signals: List<BodyPictureSignal>,
    val effectiveAt: Instant,
    val completeness: DataCompleteness,
)

fun interface BodyPicturePolicy { fun select(input: BodyPictureInput): BodyPictureModel }

data class BodyPictureInput(
    val facts: List<HealthFact>,
    val trend: List<DailyHealthPoint>,
    val effectiveAt: Instant,
    val timeZone: TimeZone,
    val availabilityOverrides: Map<BodyPictureSignalId, SignalAvailability> = emptyMap(),
)

class InitialBodyPicturePolicy(
    private val order: List<BodyPictureSignalId> = listOf(
        BodyPictureSignalId.RECOVERY, BodyPictureSignalId.STRAIN, BodyPictureSignalId.SLEEP,
    ),
) : BodyPicturePolicy {
    override fun select(input: BodyPictureInput): BodyPictureModel {
        val candidates = mapOf(
            BodyPictureSignalId.RECOVERY to recoverySignal(input),
            BodyPictureSignalId.STRAIN to strainSignal(input),
            BodyPictureSignalId.SLEEP to sleepSignal(input),
        )
        val signals = order.mapNotNull(candidates::get).map { signal ->
            input.availabilityOverrides[signal.id]?.let { state ->
                signal.copy(value = null, unit = null, evidenceClass = EvidenceClass.MISSING, state = state, baselineDelta = null, freshness = null, source = null)
            } ?: signal
        }
        val availableCount = signals.count { it.state == SignalAvailability.AVAILABLE }
        return BodyPictureModel(
            headline = "Today's body picture",
            signals = signals,
            effectiveAt = input.effectiveAt,
            completeness = when {
                availableCount == signals.size && signals.isNotEmpty() -> DataCompleteness.COMPLETE
                availableCount == 0 -> DataCompleteness.UNAVAILABLE
                else -> DataCompleteness.PARTIAL
            },
        )
    }

    private fun recoverySignal(input: BodyPictureInput): BodyPictureSignal {
        val fact = input.facts.filterIsInstance<RecoveryScoreFact>().maxByOrNull(HealthFact::observedAt)
            ?: return unavailable(BodyPictureSignalId.RECOVERY, "Recovery", SignalAvailability.INSUFFICIENT_DATA, 100)
        return scoreSignal(
            id = BodyPictureSignalId.RECOVERY,
            label = "Recovery",
            value = fact.score.toString(),
            unit = "%",
            fact = fact,
            importance = 100,
        )
    }

    private fun strainSignal(input: BodyPictureInput): BodyPictureSignal {
        val fact = input.facts.filterIsInstance<StrainScoreFact>().maxByOrNull(HealthFact::observedAt)
            ?: return unavailable(BodyPictureSignalId.STRAIN, "Strain", SignalAvailability.INSUFFICIENT_DATA, 90)
        val value = fact.score.toString()
        return scoreSignal(
            id = BodyPictureSignalId.STRAIN,
            label = "Strain",
            value = value,
            unit = null,
            fact = fact,
            importance = 90,
        )
    }

    private fun scoreSignal(
        id: BodyPictureSignalId,
        label: String,
        value: String,
        unit: String?,
        fact: HealthFact,
        importance: Int,
    ) = BodyPictureSignal(
        id = id,
        label = label,
        value = value,
        unit = unit,
        evidenceClass = EvidenceClass.CALCULATED,
        state = if (fact.freshness == Freshness.STALE) SignalAvailability.STALE else SignalAvailability.AVAILABLE,
        freshness = fact.freshness,
        source = fact.source,
        importance = importance,
    )

    private fun sleepSignal(input: BodyPictureInput): BodyPictureSignal {
        val sleep = input.facts.filterIsInstance<SleepFact>().maxByOrNull(HealthFact::observedAt)
            ?: return unavailable(BodyPictureSignalId.SLEEP, "Sleep", SignalAvailability.MISSING, 80)
        if (sleep.end.toLocalDateTime(input.timeZone).date != input.effectiveAt.toLocalDateTime(input.timeZone).date) {
            return unavailable(BodyPictureSignalId.SLEEP, "Sleep", SignalAvailability.MISSING, 80)
        }
        val baseline = when (val result = PersonalBaseline.sleep(input.trend)) {
            is BaselineResult.Available -> BaselineDelta(result.comparison.difference, "h", result.comparison.baselineSampleCount)
            is BaselineResult.InsufficientData -> null
        }
        val totalMinutes = sleep.duration.inWholeMinutes
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return BodyPictureSignal(
            id = BodyPictureSignalId.SLEEP,
            label = "Sleep",
            value = "${hours}h ${minutes}m",
            unit = null,
            evidenceClass = EvidenceClass.MEASURED,
            state = if (sleep.freshness == Freshness.STALE) SignalAvailability.STALE else SignalAvailability.AVAILABLE,
            baselineDelta = baseline,
            freshness = sleep.freshness,
            source = sleep.source,
            importance = 80,
        )
    }

    private fun unavailable(id: BodyPictureSignalId, label: String, state: SignalAvailability, importance: Int) =
        BodyPictureSignal(id, label, null, null, EvidenceClass.MISSING, state, importance = importance)
}
