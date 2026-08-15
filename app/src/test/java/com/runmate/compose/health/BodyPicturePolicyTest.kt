package com.runmate.compose.health

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BodyPicturePolicyTest {
    private val zone = ZoneId.of("Asia/Bangkok")
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
        assertEquals("provider_a", first.source?.provider)
        assertEquals("provider_b", second.source?.provider)
    }

    @Test fun policyOrderChangesWithoutChangingThePresentationModelContract() {
        val policy = InitialBodyPicturePolicy(
            listOf(BodyPictureSignalId.SLEEP, BodyPictureSignalId.RECOVERY, BodyPictureSignalId.STRAIN),
        )

        assertEquals(
            listOf(BodyPictureSignalId.SLEEP, BodyPictureSignalId.RECOVERY, BodyPictureSignalId.STRAIN),
            policy.select(input(sleep(sourceA))).signals.map(BodyPictureSignal::id),
        )
    }

    @Test fun oldSleepIsMissingForTodayEvenWhenProviderReturnedALatestRecord() {
        val oldEnd = now.minus(Duration.ofDays(2))
        val oldSleep = SleepFact(Duration.ofHours(7), oldEnd.minus(Duration.ofHours(7)), oldEnd, sourceA, Freshness.STALE)
        val signal = InitialBodyPicturePolicy().select(input(oldSleep)).signals.last()

        assertEquals(SignalAvailability.MISSING, signal.state)
        assertNull(signal.value)
    }

    @Test fun permissionStateOverridesAValueWithoutTurningItIntoMissingData() {
        val model = InitialBodyPicturePolicy().select(
            input(sleep(sourceA)).copy(
                availabilityOverrides = mapOf(BodyPictureSignalId.SLEEP to SignalAvailability.NOT_PERMITTED),
            ),
        )
        val sleep = model.signals.single { it.id == BodyPictureSignalId.SLEEP }

        assertEquals(SignalAvailability.NOT_PERMITTED, sleep.state)
        assertNull(sleep.value)
        assertNull(sleep.source)
    }

    private fun sleep(source: HealthSource): SleepFact {
        val end = Instant.parse("2026-08-15T00:00:00Z")
        return SleepFact(Duration.ofHours(7), end.minus(Duration.ofHours(7)), end, source, Freshness.FRESH)
    }

    private fun input(vararg facts: HealthFact) = BodyPictureInput(
        facts = facts.toList(),
        trend = (6 downTo 0).map { offset ->
            DailyHealthPoint(LocalDate.of(2026, 8, 15).minusDays(offset.toLong()), if (offset == 0) 7.0 else 6.5, null)
        },
        effectiveAt = now,
        zoneId = zone,
    )
}
