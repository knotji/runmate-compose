package com.runmate.compose.today

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.time.Instant
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class FocusGoal(val label: String) {
    BALANCE("Balance"),
    SLEEP("Sleep"),
    STRESS("Stress"),
    HEART("Heart health"),
    MOVEMENT("Movement"),
}

data class TodayUserState(
    val focus: FocusGoal = FocusGoal.BALANCE,
    val stress: Int? = null,
    val mood: Int? = null,
    val energy: Int? = null,
    val checkInSavedAt: Instant? = null,
)

class TodayViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val mutableState = MutableStateFlow(restore())
    val state: StateFlow<TodayUserState> = mutableState.asStateFlow()

    fun selectFocus(focus: FocusGoal) = update(mutableState.value.copy(focus = focus))
    fun setStress(value: Int) = updateRating { copy(stress = value, checkInSavedAt = null) }
    fun setMood(value: Int) = updateRating { copy(mood = value, checkInSavedAt = null) }
    fun setEnergy(value: Int) = updateRating { copy(energy = value, checkInSavedAt = null) }

    fun saveCheckIn(now: Instant = Instant.now()) {
        val current = mutableState.value
        require(listOf(current.stress, current.mood, current.energy).any { it != null }) { "At least one check-in value is required" }
        update(current.copy(checkInSavedAt = now))
    }

    private fun updateRating(transform: TodayUserState.() -> TodayUserState) {
        val next = mutableState.value.transform()
        listOfNotNull(next.stress, next.mood, next.energy).forEach {
            require(it in 1..5) { "Check-in values must be 1..5" }
        }
        update(next)
    }

    private fun update(next: TodayUserState) {
        savedStateHandle[FOCUS] = next.focus.name
        savedStateHandle[STRESS] = next.stress
        savedStateHandle[MOOD] = next.mood
        savedStateHandle[ENERGY] = next.energy
        savedStateHandle[SAVED_AT] = next.checkInSavedAt?.toString()
        mutableState.value = next
    }

    private fun restore(): TodayUserState = TodayUserState(
        focus = savedStateHandle.get<String>(FOCUS)?.let { runCatching { FocusGoal.valueOf(it) }.getOrNull() } ?: FocusGoal.BALANCE,
        stress = savedStateHandle[STRESS],
        mood = savedStateHandle[MOOD],
        energy = savedStateHandle[ENERGY],
        checkInSavedAt = savedStateHandle.get<String>(SAVED_AT)?.let { runCatching { Instant.parse(it) }.getOrNull() },
    )

    private companion object {
        const val FOCUS = "today_focus"
        const val STRESS = "today_stress"
        const val MOOD = "today_mood"
        const val ENERGY = "today_energy"
        const val SAVED_AT = "today_check_in_saved_at"
    }
}
