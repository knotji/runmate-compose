package com.runmate.compose.health

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class BodyPicturePolicyTest {
    private val zone = TimeZone.UTC
    private val now = Instant.parse("2026-08-15T03:00:00Z")
    private val sourceA = HealthSource("provider_a", "sleep.app.a", "Sleep A")
    private val sourceB = HealthSource("provider_b", "sleep.app.b", "Sleep B")

    @Test fun partialPictureKeepsUnavailableRecoveryAndStrainBesideMeasuredSleep() {
        val model = InitialBodyPicturePolicy().select(input(sleep(sourceA)))
        assertEquals(DataCompleteness.PARTIAL, model.completeness)
        assertEquals(
            listOf(SignalAvailability.NOT_CONNECTED, SignalAvailability.NOT_CONNECTED, SignalAvailability.AVAILABLE),
            model.signals.map(BodyPictureSignal::state),
        )
        assertEquals(EvidenceClass.MEASURED, model.signals.last().evidenceClass)
        assertEquals("provider_a", model.signals.last().source?.provider)
    }

    @Test fun missingSleepIsExplicitAndNeverReceivesFallbackValue() {
        val model = InitialBodyPicturePolicy().select(input())
        val sleep = model.signals.single { it.id == BodyPictureSignalId.SLEEP }
        assertEquals(SignalAvailability.MISSING, sleep.state)
        assertEquals(EvidenceClass.MISSING, sleep.evidenceClass)
        assertNull(sleep.value)
        assertEquals(DataCompleteness.UNAVAILABLE, model.completeness)
    }

    @Test fun providerIdentityDoesNotChangeSignalSemantics() {
        val first = InitialBodyPicturePolicy().select(input(sleep(sourceA))).signals.last()
        val second = InitialBodyPicturePolicy().select(input(sleep(sourceB))).signals.last()
        assertEquals(first.copy(source = second.source), second)
    }

    @Test fun policyOrderChangesWithoutChangingModelContract() {
        val policy = InitialBodyPicturePolicy(listOf(BodyPictureSignalId.SLEEP, BodyPictureSignalId.RECOVERY, BodyPictureSignalId.STRAIN))
        assertEquals(
            listOf(BodyPictureSignalId.SLEEP, BodyPictureSignalId.RECOVERY, BodyPictureSignalId.STRAIN),
            policy.select(input(sleep(sourceA))).signals.map(BodyPictureSignal::id),
        )
    }

    @Test fun oldSleepIsMissingForToday() {
        val oldEnd = now - 2.days
        val oldSleep = SleepFact(7.hours, oldEnd - 7.hours, oldEnd, sourceA, Freshness.STALE)
        val signal = InitialBodyPicturePolicy().select(input(oldSleep)).signals.last()
        assertEquals(SignalAvailability.MISSING, signal.state)
        assertNull(signal.value)
    }

    @Test fun permissionStateOverridesAValue() {
        val model = InitialBodyPicturePolicy().select(
            input(sleep(sourceA)).copy(availabilityOverrides = mapOf(BodyPictureSignalId.SLEEP to SignalAvailability.NOT_PERMITTED)),
        )
        val sleep = model.signals.single { it.id == BodyPictureSignalId.SLEEP }
        assertEquals(SignalAvailability.NOT_PERMITTED, sleep.state)
        assertNull(sleep.value)
        assertNull(sleep.source)
    }

    private fun sleep(source: HealthSource): SleepFact {
        val end = Instant.parse("2026-08-15T00:00:00Z")
        return SleepFact(7.hours, end - 7.hours, end, source, Freshness.FRESH)
    }

    private fun input(vararg facts: HealthFact) = BodyPictureInput(
        facts = facts.toList(),
        trend = (9..15).map { day -> DailyHealthPoint(LocalDate(2026, 8, day), if (day == 15) 7.0 else 6.5, null) },
        effectiveAt = now,
        timeZone = zone,
    )
}
