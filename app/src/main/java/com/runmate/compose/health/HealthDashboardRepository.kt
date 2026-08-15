package com.runmate.compose.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HealthDashboardData(
    val sleep: SleepSummary?,
    val heartRate: HeartRateSummary?,
    val hrv: String,
    val respiratoryRate: String,
    val workout: String,
    val sevenDayTrend: List<DailyHealthPoint>,
)

data class DailyHealthPoint(
    val date: LocalDate,
    val sleepHours: Double?,
    val averageHeartRate: Double?,
)

sealed interface HealthLoadResult {
    data object Unavailable : HealthLoadResult
    data class PermissionRequired(val missing: Set<String>) : HealthLoadResult
    data class Success(val data: HealthDashboardData) : HealthLoadResult
}

class HealthDashboardRepository(private val context: Context) {
    companion object {
        val permissions: Set<String> = setOf(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        )
    }

    suspend fun load(): HealthLoadResult {
        if (HealthConnectClient.getSdkStatus(context) != HealthConnectClient.SDK_AVAILABLE) {
            return HealthLoadResult.Unavailable
        }
        val client = HealthConnectClient.getOrCreate(context)
        val missing = permissions - client.permissionController.getGrantedPermissions()
        if (missing.isNotEmpty()) return HealthLoadResult.PermissionRequired(missing)

        val now = Instant.now()
        val range = TimeRangeFilter.between(now.minus(Duration.ofDays(30)), now)
        val sleepRecords = client.readRecords(ReadRecordsRequest<SleepSessionRecord>(range, pageSize = 100))
            .records
        val heartRateRecords = client.readRecords(ReadRecordsRequest<HeartRateRecord>(range, pageSize = 1000))
            .records
        val sleep = sleepRecords.maxByOrNull { it.endTime }
        val heartRate = heartRateRecords.maxByOrNull { it.endTime }
        val hrv = client.readRecords(ReadRecordsRequest<HeartRateVariabilityRmssdRecord>(range, pageSize = 100))
            .records.maxByOrNull { it.time }
        val respiratoryRate = client.readRecords(ReadRecordsRequest<RespiratoryRateRecord>(range, pageSize = 100))
            .records.maxByOrNull { it.time }
        val workout = client.readRecords(ReadRecordsRequest<ExerciseSessionRecord>(range, pageSize = 30))
            .records.maxByOrNull { it.endTime }

        return HealthLoadResult.Success(
            HealthDashboardData(
                sleep?.let { SleepSummary(Duration.between(it.startTime, it.endTime), it.startTime, it.endTime) },
                heartRate?.samples?.maxByOrNull { it.time }?.let { HeartRateSummary(it.beatsPerMinute, it.time) },
                hrv?.let { "%.1f ms • %s".format(it.heartRateVariabilityMillis, HealthDisplayFormatter.time(it.time)) }
                    ?: "No HRV data in the last 30 days",
                respiratoryRate?.let { "%.1f breaths/min • %s".format(it.rate, HealthDisplayFormatter.time(it.time)) }
                    ?: "No respiratory-rate data in the last 30 days",
                workout?.let(::formatWorkout) ?: "No workout data in the last 30 days",
                buildSevenDayTrend(sleepRecords, heartRateRecords),
            ),
        )
    }

    private fun buildSevenDayTrend(
        sleepRecords: List<SleepSessionRecord>,
        heartRateRecords: List<HeartRateRecord>,
    ): List<DailyHealthPoint> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val sleepByDate = sleepRecords.groupBy { it.endTime.atZone(zone).toLocalDate() }
        val heartSamplesByDate = heartRateRecords.flatMap { it.samples }
            .groupBy { it.time.atZone(zone).toLocalDate() }
        return (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val sleepMinutes = sleepByDate[date]?.sumOf { Duration.between(it.startTime, it.endTime).toMinutes() }
            val heartSamples = heartSamplesByDate[date].orEmpty()
            DailyHealthPoint(
                date = date,
                sleepHours = sleepMinutes?.div(60.0),
                averageHeartRate = heartSamples.takeIf { it.isNotEmpty() }?.map { it.beatsPerMinute.toDouble() }?.average(),
            )
        }
    }

    private fun formatWorkout(record: ExerciseSessionRecord): String {
        val minutes = Duration.between(record.startTime, record.endTime).toMinutes()
        val title = record.title?.takeIf(String::isNotBlank) ?: "Exercise type ${record.exerciseType}"
        return "$title • ${minutes}m • ${HealthDisplayFormatter.time(record.endTime)}"
    }
}
