package com.runmate.compose.health

data class BaselineComparison(
    val current: Double,
    val baselineAverage: Double,
    val difference: Double,
    val baselineSampleCount: Int,
)

sealed interface BaselineResult {
    data class Available(val comparison: BaselineComparison) : BaselineResult
    data class InsufficientData(
        val baselineSampleCount: Int,
        val requiredBaselineSamples: Int,
        val currentAvailable: Boolean,
    ) : BaselineResult
}

object PersonalBaseline {
    const val MINIMUM_BASELINE_DAYS = 3

    fun sleep(points: List<DailyHealthPoint>): BaselineResult =
        compare(points.map { it.sleepHours })

    fun heartRate(points: List<DailyHealthPoint>): BaselineResult =
        compare(points.map { it.averageHeartRate })

    internal fun compare(values: List<Double?>, minimumSamples: Int = MINIMUM_BASELINE_DAYS): BaselineResult {
        require(minimumSamples > 0) { "Minimum samples must be positive" }
        val current = values.lastOrNull()
        val baseline = values.dropLast(1).filterNotNull().filter(Double::isFinite)
        if (current == null || !current.isFinite() || baseline.size < minimumSamples) {
            return BaselineResult.InsufficientData(
                baselineSampleCount = baseline.size,
                requiredBaselineSamples = minimumSamples,
                currentAvailable = current?.isFinite() == true,
            )
        }
        val average = baseline.average()
        return BaselineResult.Available(
            BaselineComparison(
                current = current,
                baselineAverage = average,
                difference = current - average,
                baselineSampleCount = baseline.size,
            ),
        )
    }
}
