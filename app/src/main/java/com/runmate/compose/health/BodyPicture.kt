package com.runmate.compose.health

import java.time.Instant
import java.time.ZoneId

enum class BodyPictureSignalId { RECOVERY, STRAIN, SLEEP, RESTING_HEART_RATE, STRESS, MOVEMENT }

enum class SignalAvailability {
    AVAILABLE,
    MISSING,
    NOT_PERMITTED,
    NOT_SUPPORTED,
    NOT_CONNECTED,
    STALE,
    INSUFFICIENT_DATA,
}

enum class DataCompleteness { COMPLETE, PARTIAL, UNAVAILABLE }

data class BaselineDelta(
    val difference: Double,
    val unit: String,
    val sampleCount: Int,
)

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

fun interface BodyPicturePolicy {
    fun select(input: BodyPictureInput): BodyPictureModel
}

data class BodyPictureInput(
    val facts: List<HealthFact>,
    val trend: List<DailyHealthPoint>,
    val effectiveAt: Instant,
    val zoneId: ZoneId,
    val availabilityOverrides: Map<BodyPictureSignalId, SignalAvailability> = emptyMap(),
)

class InitialBodyPicturePolicy(
    private val order: List<BodyPictureSignalId> = listOf(
        BodyPictureSignalId.RECOVERY,
        BodyPictureSignalId.STRAIN,
        BodyPictureSignalId.SLEEP,
    ),
) : BodyPicturePolicy {
    override fun select(input: BodyPictureInput): BodyPictureModel {
        val candidates = mapOf(
            BodyPictureSignalId.RECOVERY to unavailableSignal(
                BodyPictureSignalId.RECOVERY,
                "Recovery",
                SignalAvailability.NOT_CONNECTED,
                100,
            ),
            BodyPictureSignalId.STRAIN to unavailableSignal(
                BodyPictureSignalId.STRAIN,
                "Strain",
                SignalAvailability.NOT_CONNECTED,
                90,
            ),
            BodyPictureSignalId.SLEEP to sleepSignal(input),
        )
        val signals = order.mapNotNull(candidates::get).map { signal ->
            input.availabilityOverrides[signal.id]?.let { overriddenState ->
                signal.copy(
                    value = null,
                    unit = null,
                    evidenceClass = EvidenceClass.MISSING,
                    state = overriddenState,
                    baselineDelta = null,
                    freshness = null,
                    source = null,
                )
            } ?: signal
        }
        val availableCount = signals.count { it.state == SignalAvailability.AVAILABLE }
        val completeness = when {
            availableCount == signals.size && signals.isNotEmpty() -> DataCompleteness.COMPLETE
            availableCount == 0 -> DataCompleteness.UNAVAILABLE
            else -> DataCompleteness.PARTIAL
        }
        return BodyPictureModel(
            headline = "Today's body picture",
            signals = signals,
            effectiveAt = input.effectiveAt,
            completeness = completeness,
        )
    }

    private fun sleepSignal(input: BodyPictureInput): BodyPictureSignal {
        val sleep = input.facts.filterIsInstance<SleepFact>().maxByOrNull(HealthFact::observedAt)
            ?: return unavailableSignal(BodyPictureSignalId.SLEEP, "Sleep", SignalAvailability.MISSING, 80)
        val isToday = sleep.end.atZone(input.zoneId).toLocalDate() == input.effectiveAt.atZone(input.zoneId).toLocalDate()
        if (!isToday) return unavailableSignal(BodyPictureSignalId.SLEEP, "Sleep", SignalAvailability.MISSING, 80)

        val baseline = when (val result = PersonalBaseline.sleep(input.trend)) {
            is BaselineResult.Available -> BaselineDelta(
                difference = result.comparison.difference,
                unit = "h",
                sampleCount = result.comparison.baselineSampleCount,
            )
            is BaselineResult.InsufficientData -> null
        }
        return BodyPictureSignal(
            id = BodyPictureSignalId.SLEEP,
            label = "Sleep",
            value = "%.1f".format(sleep.duration.toMinutes() / 60.0),
            unit = "h",
            evidenceClass = EvidenceClass.MEASURED,
            state = if (sleep.freshness == Freshness.STALE) SignalAvailability.STALE else SignalAvailability.AVAILABLE,
            baselineDelta = baseline,
            freshness = sleep.freshness,
            source = sleep.source,
            importance = 80,
        )
    }

    private fun unavailableSignal(
        id: BodyPictureSignalId,
        label: String,
        state: SignalAvailability,
        importance: Int,
    ) = BodyPictureSignal(
        id = id,
        label = label,
        value = null,
        unit = null,
        evidenceClass = EvidenceClass.MISSING,
        state = state,
        importance = importance,
    )
}
