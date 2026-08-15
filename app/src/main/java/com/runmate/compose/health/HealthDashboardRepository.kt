package com.runmate.compose.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class HealthDashboardData(
    val sleep: SleepSummary?,
    val heartRate: HeartRateSummary?,
    val hrv: HrvSummary?,
    val respiratoryRate: RespiratoryRateSummary?,
    val latestActivity: ActivitySummary?,
    val stepsToday: DailyStepsSummary?,
    val sevenDayTrend: List<DailyHealthPoint>,
    val facts: List<HealthFact>,
    val syncedAt: Instant,
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
            HealthPermission.getReadPermission(StepsRecord::class),
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
        val latestActivity = client.readRecords(ReadRecordsRequest<ExerciseSessionRecord>(range, pageSize = 30))
            .records.maxByOrNull { it.endTime }
        val zone = ZoneId.systemDefault()
        val startOfToday = LocalDate.now(zone).atStartOfDay(zone).toInstant()
        val stepsTotal = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfToday, now),
            ),
        )[StepsRecord.COUNT_TOTAL]

        val sleepSummary = sleep?.let { SleepSummary(Duration.between(it.startTime, it.endTime), it.startTime, it.endTime, origin(it.metadata.dataOrigin.packageName)) }
        val heartRateSummary = heartRate?.samples?.maxByOrNull { it.time }
            ?.let { HeartRateSummary(it.beatsPerMinute, it.time, origin(heartRate.metadata.dataOrigin.packageName)) }
        val hrvSummary = hrv?.let { HrvSummary(it.heartRateVariabilityMillis, it.time, origin(it.metadata.dataOrigin.packageName)) }
        val respiratorySummary = respiratoryRate?.let { RespiratoryRateSummary(it.rate, it.time, origin(it.metadata.dataOrigin.packageName)) }
        val activitySummary = latestActivity?.let {
            ActivitySummary(
                typeCode = it.exerciseType,
                title = it.title,
                duration = Duration.between(it.startTime, it.endTime),
                startedAt = it.startTime,
                endedAt = it.endTime,
                origin = origin(it.metadata.dataOrigin.packageName),
            )
        }
        val stepsSummary = stepsTotal?.let { DailyStepsSummary(it, startOfToday, now) }
        val trend = buildSevenDayTrend(sleepRecords, heartRateRecords)
        return HealthLoadResult.Success(
            HealthDashboardData(
                sleepSummary,
                heartRateSummary,
                hrvSummary,
                respiratorySummary,
                activitySummary,
                stepsSummary,
                trend,
                facts = buildFacts(sleepSummary, heartRateSummary, hrvSummary, respiratorySummary, activitySummary, stepsSummary, now),
                syncedAt = now,
            ),
        )
    }

    private fun buildFacts(
        sleep: SleepSummary?,
        heartRate: HeartRateSummary?,
        hrv: HrvSummary?,
        respiratoryRate: RespiratoryRateSummary?,
        activity: ActivitySummary?,
        steps: DailyStepsSummary?,
        assessedAt: Instant,
    ): List<HealthFact> = buildList {
        sleep?.let {
            add(SleepFact(it.duration, it.startedAt, it.endedAt, source(it.origin), freshness(it.endedAt, assessedAt, Duration.ofHours(36))))
        }
        heartRate?.let {
            add(HeartRateFact(it.beatsPerMinute, it.measuredAt, source(it.origin), freshness(it.measuredAt, assessedAt, Duration.ofHours(24))))
        }
        hrv?.let {
            add(HrvFact(it.rmssdMillis, it.measuredAt, source(it.origin), freshness(it.measuredAt, assessedAt, Duration.ofHours(36))))
        }
        respiratoryRate?.let {
            add(RespiratoryRateFact(it.breathsPerMinute, it.measuredAt, source(it.origin), freshness(it.measuredAt, assessedAt, Duration.ofHours(36))))
        }
        activity?.let {
            add(ActivityFact(it.typeCode, it.title, it.duration, it.startedAt, it.endedAt, source(it.origin), freshness(it.endedAt, assessedAt, Duration.ofDays(2))))
        }
        steps?.let {
            add(StepsFact(it.count, it.startedAt, it.endedAt, HealthSource("health_connect_aggregate"), Freshness.FRESH))
        }
    }

    private fun source(origin: SignalOrigin?): HealthSource = HealthSource(
        provider = "health_connect",
        originId = origin?.packageName,
        originLabel = origin?.appLabel,
    )

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

    private fun origin(packageName: String): SignalOrigin {
        val label = runCatching {
            val info = context.packageManager.getApplicationInfo(packageName, 0)
            context.packageManager.getApplicationLabel(info).toString()
        }.getOrDefault(packageName)
        return SignalOrigin(packageName, label)
    }

}
