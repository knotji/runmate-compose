package com.runmate.compose.health

import kotlinx.datetime.LocalDate

data class DailyHealthPoint(
    val date: LocalDate,
    val sleepHours: Double?,
    val averageHeartRate: Double?,
)
