package com.runmate.compose.health

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

internal data class SleepInterval(
    val start: Instant,
    val end: Instant,
    val sourceId: String,
) {
    init { require(end >= start) { "Sleep interval end must not precede start" } }
}

internal data class AggregatedSleep(
    val duration: Duration,
    val startedAt: Instant,
    val endedAt: Instant,
    val sourceId: String,
)

internal object SleepAggregation {
    private val maximumContinuationGap: Duration = Duration.ofMinutes(90)

    fun longestForWakeDate(
        intervals: List<SleepInterval>,
        wakeDate: LocalDate,
        zoneId: ZoneId,
    ): AggregatedSleep? = intervals
        .filter { it.end.atZone(zoneId).toLocalDate() == wakeDate }
        .groupBy(SleepInterval::sourceId)
        .values
        .flatMap(::clusters)
        .maxWithOrNull(compareBy<AggregatedSleep> { it.duration }.thenBy { it.endedAt })

    private fun clusters(sourceIntervals: List<SleepInterval>): List<AggregatedSleep> {
        val sorted = sourceIntervals.sortedWith(compareBy(SleepInterval::start).thenBy(SleepInterval::end))
        if (sorted.isEmpty()) return emptyList()

        val result = mutableListOf<AggregatedSleep>()
        var clusterStart = sorted.first().start
        var clusterEnd = sorted.first().end
        var measuredDuration = Duration.between(clusterStart, clusterEnd)
        val sourceId = sorted.first().sourceId

        fun finishCluster() {
            result += AggregatedSleep(measuredDuration, clusterStart, clusterEnd, sourceId)
        }

        sorted.drop(1).forEach { interval ->
            val continuationDeadline = clusterEnd.plus(maximumContinuationGap)
            if (interval.start <= continuationDeadline) {
                if (interval.end > clusterEnd) {
                    val uncoveredStart = maxOf(interval.start, clusterEnd)
                    measuredDuration = measuredDuration.plus(Duration.between(uncoveredStart, interval.end))
                    clusterEnd = interval.end
                }
            } else {
                finishCluster()
                clusterStart = interval.start
                clusterEnd = interval.end
                measuredDuration = Duration.between(interval.start, interval.end)
            }
        }
        finishCluster()
        return result
    }
}
