package com.runmate.compose.health

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PersonalBaselineTest {
    @Test fun currentDayIsExcludedFromBaseline() {
        val result = PersonalBaseline.compare(listOf(6.0, 7.0, 8.0, 9.0)) as BaselineResult.Available
        assertEquals(7.0, result.comparison.baselineAverage, 0.001)
        assertEquals(2.0, result.comparison.difference, 0.001)
        assertEquals(3, result.comparison.baselineSampleCount)
    }

    @Test fun missingPreviousDaysDoNotBecomeZero() {
        val result = PersonalBaseline.compare(listOf(6.0, null, 8.0, 7.0, 9.0)) as BaselineResult.Available
        assertEquals(7.0, result.comparison.baselineAverage, 0.001)
        assertEquals(3, result.comparison.baselineSampleCount)
    }

    @Test fun insufficientBaselineIsExplicit() {
        val result = PersonalBaseline.compare(listOf(null, 7.0, null, 8.0)) as BaselineResult.InsufficientData
        assertEquals(1, result.baselineSampleCount)
        assertEquals(3, result.requiredBaselineSamples)
        assertTrue(result.currentAvailable)
    }

    @Test fun missingCurrentValueIsExplicit() {
        val result = PersonalBaseline.compare(listOf(6.0, 7.0, 8.0, null)) as BaselineResult.InsufficientData
        assertEquals(3, result.baselineSampleCount)
        assertFalse(result.currentAvailable)
    }

    @Test fun nonFiniteValuesAreNotEvidence() {
        val result = PersonalBaseline.compare(listOf(6.0, Double.NaN, 8.0, 9.0)) as BaselineResult.InsufficientData
        assertEquals(2, result.baselineSampleCount)
    }
}
