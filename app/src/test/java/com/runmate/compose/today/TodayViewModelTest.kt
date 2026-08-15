package com.runmate.compose.today

import androidx.lifecycle.SavedStateHandle
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test

class TodayViewModelTest {
    @Test fun focusAndCheckInRestoreFromSavedState() {
        val handle = SavedStateHandle()
        val first = TodayViewModel(handle)
        first.selectFocus(FocusGoal.STRESS)
        first.setStress(4)
        first.setMood(2)
        first.setEnergy(3)
        first.saveCheckIn(Instant.parse("2026-08-15T04:00:00Z"))

        val restored = TodayViewModel(handle).state.value
        assertEquals(FocusGoal.STRESS, restored.focus)
        assertEquals(4, restored.stress)
        assertEquals(2, restored.mood)
        assertEquals(3, restored.energy)
        assertEquals(Instant.parse("2026-08-15T04:00:00Z"), restored.checkInSavedAt)
    }

    @Test fun editingAValueClearsSavedMarker() {
        val model = TodayViewModel(SavedStateHandle())
        model.setMood(4)
        model.saveCheckIn(Instant.parse("2026-08-15T04:00:00Z"))
        model.setMood(5)

        assertNull(model.state.value.checkInSavedAt)
    }

    @Test fun ratingOutsideScaleIsRejected() {
        val model = TodayViewModel(SavedStateHandle())
        assertThrows(IllegalArgumentException::class.java) { model.setStress(0) }
        assertThrows(IllegalArgumentException::class.java) { model.setEnergy(6) }
    }

    @Test fun emptyCheckInCannotBeSaved() {
        val model = TodayViewModel(SavedStateHandle())
        assertThrows(IllegalArgumentException::class.java) { model.saveCheckIn() }
    }
}
