package com.runmate.compose.health

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class TodayDecisionLogicTest {
    private val now = Instant.parse("2026-08-15T00:00:00Z")
    private val source = HealthSource("wholemate_derived_sleeping_hr")

    @Test fun strongerSleepingHeartRateDeviationRanksBeforeNeutralSleep() {
        val facts = listOf(
            SleepingHeartRateFact(53.27, 405, now, source, Freshness.FRESH),
            SleepFact(6.72.hours, now - 6.72.hours, now, source, Freshness.FRESH),
        )
        val ranked = ShapingFactRanker.rank(facts, trend())

        assertEquals(listOf(ShapingSignal.SLEEPING_HEART_RATE, ShapingSignal.SLEEP_DURATION), ranked.map { it.signal })
        assertEquals(ShapingStrength.STRONG, ranked[0].strength)
        assertEquals(ShapingStrength.NEUTRAL, ranked[1].strength)
        assertTrue(ranked[0].deviationPercent > 4.0)
    }

    @Test fun incompleteBaselineDoesNotCreateAnExplanation() {
        val facts = listOf(SleepingHeartRateFact(53.27, 405, now, source, Freshness.FRESH))
        assertTrue(ShapingFactRanker.rank(facts, trend().takeLast(5)).isEmpty())
    }

    @Test fun nextActionIsTraceableToEvidenceState() {
        val strong = ShapingFact(
            ShapingSignal.SLEEPING_HEART_RATE, 53.0, 51.0, 4.0,
            BaselineDirection.ABOVE, ShapingStrength.STRONG, Freshness.FRESH,
            EvidenceClass.CALCULATED, 405,
        )
        assertEquals(NextAction.REVIEW_DETAILS, NextActionPolicy.select(listOf(strong), null))
        assertEquals(NextAction.REFRESH_TODAY, NextActionPolicy.select(listOf(strong.copy(freshness = Freshness.STALE)), null))
        assertEquals(NextAction.COMPLETE_HEALTH_ACCESS, NextActionPolicy.select(emptyList(), null, healthAccessIncomplete = true))
        assertEquals(NextAction.KEEP_USUAL_PLAN, NextActionPolicy.select(emptyList(), null))
    }

    private fun trend() = (9..15).map { day ->
        DailyHealthPoint(
            date = LocalDate(2026, 8, day),
            sleepHours = if (day == 15) 6.72 else 6.67,
            averageHeartRate = null,
            sleepingHeartRateBpm = if (day == 15) 53.27 else 51.11,
        )
    }
}
