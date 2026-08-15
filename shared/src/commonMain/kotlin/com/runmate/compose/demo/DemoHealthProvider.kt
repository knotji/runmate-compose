package com.runmate.compose.demo

import com.runmate.compose.core.state.LoadState
import com.runmate.compose.health.BodyPictureInput
import com.runmate.compose.health.BodyPictureModel
import com.runmate.compose.health.BodyPictureSignal
import com.runmate.compose.health.BodyPictureSignalId
import com.runmate.compose.health.DailyHealthPoint
import com.runmate.compose.health.DataCompleteness
import com.runmate.compose.health.EvidenceClass
import com.runmate.compose.health.Freshness
import com.runmate.compose.health.HealthSource
import com.runmate.compose.health.InitialBodyPicturePolicy
import com.runmate.compose.health.SignalAvailability
import com.runmate.compose.health.SleepFact
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

enum class DemoTodayScenario { AVAILABLE, PARTIAL, MISSING, LOADING, ERROR }

/** Development fixtures only. This provider never represents Health Connect or production data. */
class DemoHealthProvider {
    private val now = Instant.parse("2026-08-15T03:00:00Z")
    private val source = HealthSource("wholemate_demo", "demo.fixture", "Demo health data")
    private val trend = (9..15).map { day ->
        DailyHealthPoint(LocalDate(2026, 8, day), if (day == 15) 7.4 else 6.5 + ((day % 3) * 0.2), 61.0 + (day % 2))
    }

    fun load(scenario: DemoTodayScenario): LoadState<BodyPictureModel> = when (scenario) {
        DemoTodayScenario.AVAILABLE -> LoadState.Ready(available(), now.toEpochMilliseconds())
        DemoTodayScenario.PARTIAL -> LoadState.Ready(partial(), now.toEpochMilliseconds())
        DemoTodayScenario.MISSING -> LoadState.Ready(missing(), now.toEpochMilliseconds())
        DemoTodayScenario.LOADING -> LoadState.Loading(partial())
        DemoTodayScenario.ERROR -> LoadState.Failed("Demo provider could not load", partial(), retryable = true)
    }

    private fun partial(): BodyPictureModel {
        val end = Instant.parse("2026-08-15T00:10:00Z")
        return InitialBodyPicturePolicy().select(
            BodyPictureInput(
                facts = listOf(SleepFact(7.hours + 24.minutes, end - (7.hours + 24.minutes), end, source, Freshness.FRESH)),
                trend = trend,
                effectiveAt = now,
                timeZone = TimeZone.UTC,
            ),
        )
    }

    private fun missing(): BodyPictureModel = InitialBodyPicturePolicy().select(
        BodyPictureInput(emptyList(), trend.map { it.copy(sleepHours = null) }, now, TimeZone.UTC),
    )

    private fun available(): BodyPictureModel = BodyPictureModel(
        headline = "Today's body picture",
        signals = listOf(
            signal(BodyPictureSignalId.SLEEP, "Sleep", "7.4", "h", 100),
            signal(BodyPictureSignalId.RESTING_HEART_RATE, "Resting HR", "58", "bpm", 90),
            signal(BodyPictureSignalId.MOVEMENT, "Movement", "6,420", "steps", 80),
        ),
        effectiveAt = now,
        completeness = DataCompleteness.COMPLETE,
    )

    private fun signal(id: BodyPictureSignalId, label: String, value: String, unit: String, importance: Int) =
        BodyPictureSignal(id, label, value, unit, EvidenceClass.MEASURED, SignalAvailability.AVAILABLE, freshness = Freshness.FRESH, source = source, importance = importance)
}
