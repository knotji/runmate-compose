package com.runmate.compose.health

import kotlinx.datetime.LocalDate

data class DailyHealthPoint(
    val date: LocalDate,
    val sleepHours: Double?,
    val averageHeartRate: Double?,
    val hrvRmssdMillis: Double? = null,
    val restingHeartRateBpm: Double? = null,
    val sleepingHeartRateBpm: Double? = null,
)
