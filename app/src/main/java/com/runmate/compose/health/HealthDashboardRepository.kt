package com.runmate.compose.health

import android.content.Context
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.request.AggregateRequest
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.datetime.LocalDate as KotlinLocalDate
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant as KotlinInstant

data class HealthDashboardData(
    val sleep: SleepSummary?,
    val heartRate: HeartRateSummary?,
    val restingHeartRate: RestingHeartRateSummary?,
    val sleepingHeartRate: SleepingHeartRateSummary?,
    val hrv: HrvSummary?,
    val respiratoryRate: RespiratoryRateSummary?,
    val latestActivity: ActivitySummary?,
    val stepsToday: DailyStepsSummary?,
    val sevenDayTrend: List<DailyHealthPoint>,
    val facts: List<HealthFact>,
    val syncedAt: Instant,
)

sealed interface HealthLoadResult {
    data object Unavailable : HealthLoadResult
    data class PermissionRequired(val missing: Set<String>) : HealthLoadResult
    data class Success(val data: HealthDashboardData) : HealthLoadResult
}

class HealthDashboardRepository(private val context: Context) {
    companion object {
        private const val MINIMUM_SLEEPING_HEART_RATE_SAMPLES = 30
        val permissions: Set<String> = setOf(
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
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
        val restingHeartRateRecords = client.readRecords(ReadRecordsRequest<RestingHeartRateRecord>(range, pageSize = 100))
            .records
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val sleep = SleepAggregation.longestForWakeDate(
            intervals = sleepRecords.map {
                SleepInterval(it.startTime, it.endTime, it.metadata.dataOrigin.packageName)
            },
            wakeDate = today,
            zoneId = zone,
        )
        val heartRate = heartRateRecords.maxByOrNull { it.endTime }
        val restingHeartRate = restingHeartRateRecords.maxByOrNull { it.time }
        val hrvRecords = client.readRecords(ReadRecordsRequest<HeartRateVariabilityRmssdRecord>(range, pageSize = 100))
            .records
        val hrv = hrvRecords.maxByOrNull { it.time }
        val respiratoryRate = client.readRecords(ReadRecordsRequest<RespiratoryRateRecord>(range, pageSize = 100))
            .records.maxByOrNull { it.time }
        val latestActivity = client.readRecords(ReadRecordsRequest<ExerciseSessionRecord>(range, pageSize = 30))
            .records.maxByOrNull { it.endTime }
        val startOfToday = today.atStartOfDay(zone).toInstant()
        val stepsTotal = client.aggregate(
            AggregateRequest(
                metrics = setOf(StepsRecord.COUNT_TOTAL),
                timeRangeFilter = TimeRangeFilter.between(startOfToday, now),
            ),
        )[StepsRecord.COUNT_TOTAL]

        val sleepSummary = sleep?.let { SleepSummary(it.duration, it.startedAt, it.endedAt, origin(it.sourceId)) }
        val heartRateSummary = heartRate?.samples?.maxByOrNull { it.time }
            ?.let { HeartRateSummary(it.beatsPerMinute, it.time, origin(heartRate.metadata.dataOrigin.packageName)) }
        val restingHeartRateSummary = restingHeartRate?.let {
            RestingHeartRateSummary(it.beatsPerMinute, it.time, origin(it.metadata.dataOrigin.packageName))
        }
        val sleepingHeartSamples = sleep?.let { interval ->
            heartRateRecords.flatMap { it.samples }.filter { it.time >= interval.startedAt && it.time <= interval.endedAt }
        }.orEmpty()
        val sleepingHeartRateSummary = sleepingHeartSamples.takeIf { it.size >= MINIMUM_SLEEPING_HEART_RATE_SAMPLES }?.let { samples ->
            SleepingHeartRateSummary(samples.map { it.beatsPerMinute }.average(), samples.size, sleep!!.endedAt)
        }
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
        val trend = buildSevenDayTrend(sleepRecords, heartRateRecords, hrvRecords, restingHeartRateRecords)
        return HealthLoadResult.Success(
            HealthDashboardData(
                sleepSummary,
                heartRateSummary,
                restingHeartRateSummary,
                sleepingHeartRateSummary,
                hrvSummary,
                respiratorySummary,
                activitySummary,
                stepsSummary,
                trend,
                facts = buildFacts(sleepSummary, heartRateSummary, restingHeartRateSummary, sleepingHeartRateSummary, hrvSummary, respiratorySummary, activitySummary, stepsSummary, now),
                syncedAt = now,
            ),
        )
    }

    private fun buildFacts(
        sleep: SleepSummary?,
        heartRate: HeartRateSummary?,
        restingHeartRate: RestingHeartRateSummary?,
        sleepingHeartRate: SleepingHeartRateSummary?,
        hrv: HrvSummary?,
        respiratoryRate: RespiratoryRateSummary?,
        activity: ActivitySummary?,
        steps: DailyStepsSummary?,
        assessedAt: Instant,
    ): List<HealthFact> = buildList {
        sleep?.let {
            add(SleepFact(it.duration.toMillis().milliseconds, it.startedAt.toSharedInstant(), it.endedAt.toSharedInstant(), source(it.origin), assessFreshness(it.endedAt.toSharedInstant(), assessedAt.toSharedInstant(), Duration.ofHours(36).toMillis().milliseconds)))
        }
        heartRate?.let {
            add(HeartRateFact(it.beatsPerMinute, it.measuredAt.toSharedInstant(), source(it.origin), assessFreshness(it.measuredAt.toSharedInstant(), assessedAt.toSharedInstant(), Duration.ofHours(24).toMillis().milliseconds)))
        }
        restingHeartRate?.let {
            add(RestingHeartRateFact(it.beatsPerMinute, it.measuredAt.toSharedInstant(), source(it.origin), assessFreshness(it.measuredAt.toSharedInstant(), assessedAt.toSharedInstant(), Duration.ofHours(36).toMillis().milliseconds)))
        }
        sleepingHeartRate?.let {
            add(SleepingHeartRateFact(it.beatsPerMinute, it.sampleCount, it.measuredUntil.toSharedInstant(), HealthSource("wholemate_derived_sleeping_hr"), assessFreshness(it.measuredUntil.toSharedInstant(), assessedAt.toSharedInstant(), Duration.ofHours(36).toMillis().milliseconds)))
        }
        hrv?.let {
            add(HrvFact(it.rmssdMillis, it.measuredAt.toSharedInstant(), source(it.origin), assessFreshness(it.measuredAt.toSharedInstant(), assessedAt.toSharedInstant(), Duration.ofHours(36).toMillis().milliseconds)))
        }
        respiratoryRate?.let {
            add(RespiratoryRateFact(it.breathsPerMinute, it.measuredAt.toSharedInstant(), source(it.origin), assessFreshness(it.measuredAt.toSharedInstant(), assessedAt.toSharedInstant(), Duration.ofHours(36).toMillis().milliseconds)))
        }
        activity?.let {
            add(ActivityFact(it.typeCode, it.title, it.duration.toMillis().milliseconds, it.startedAt.toSharedInstant(), it.endedAt.toSharedInstant(), source(it.origin), assessFreshness(it.endedAt.toSharedInstant(), assessedAt.toSharedInstant(), Duration.ofDays(2).toMillis().milliseconds)))
        }
        steps?.let {
            add(StepsFact(it.count, it.startedAt.toSharedInstant(), it.endedAt.toSharedInstant(), HealthSource("health_connect_aggregate"), Freshness.FRESH))
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
        hrvRecords: List<HeartRateVariabilityRmssdRecord>,
        restingHeartRateRecords: List<RestingHeartRateRecord>,
    ): List<DailyHealthPoint> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val sleepIntervals = sleepRecords.map {
            SleepInterval(it.startTime, it.endTime, it.metadata.dataOrigin.packageName)
        }
        val allHeartSamples = heartRateRecords.flatMap { it.samples }
        val heartSamplesByDate = allHeartSamples.groupBy { it.time.atZone(zone).toLocalDate() }
        val hrvByDate = hrvRecords.groupBy { it.time.atZone(zone).toLocalDate() }
        val restingHeartRateByDate = restingHeartRateRecords.groupBy { it.time.atZone(zone).toLocalDate() }
        return (6 downTo 0).map { offset ->
            val date = today.minusDays(offset.toLong())
            val sleepMinutes = SleepAggregation.longestForWakeDate(
                intervals = sleepIntervals,
                wakeDate = date,
                zoneId = zone,
            )?.duration?.toMinutes()
            val sleepInterval = SleepAggregation.longestForWakeDate(sleepIntervals, date, zone)
            val sleepingHeartSamples = sleepInterval?.let { interval ->
                allHeartSamples.filter { it.time >= interval.startedAt && it.time <= interval.endedAt }
            }.orEmpty()
            val heartSamples = heartSamplesByDate[date].orEmpty()
            DailyHealthPoint(
                date = KotlinLocalDate(date.year, date.monthValue, date.dayOfMonth),
                sleepHours = sleepMinutes?.div(60.0),
                averageHeartRate = heartSamples.takeIf { it.isNotEmpty() }?.map { it.beatsPerMinute.toDouble() }?.average(),
                hrvRmssdMillis = hrvByDate[date]?.maxByOrNull { it.time }?.heartRateVariabilityMillis,
                restingHeartRateBpm = restingHeartRateByDate[date]?.maxByOrNull { it.time }?.beatsPerMinute?.toDouble(),
                sleepingHeartRateBpm = sleepingHeartSamples.takeIf { it.size >= MINIMUM_SLEEPING_HEART_RATE_SAMPLES }
                    ?.map { it.beatsPerMinute }?.average(),
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

    private fun Instant.toSharedInstant(): KotlinInstant = KotlinInstant.fromEpochMilliseconds(toEpochMilli())

}
