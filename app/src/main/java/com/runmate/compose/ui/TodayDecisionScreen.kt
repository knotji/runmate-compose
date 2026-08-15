package com.runmate.compose.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import com.runmate.compose.health.DailyHealthPoint
import com.runmate.compose.health.HealthDashboardData
import com.runmate.compose.health.HealthDashboardUiState
import com.runmate.compose.health.HealthDashboardViewModel
import com.runmate.compose.health.HealthDisplayFormatter
import com.runmate.compose.health.BodyPictureModel
import com.runmate.compose.health.BodyPictureSignal
import com.runmate.compose.health.BodyPictureSignalId
import com.runmate.compose.health.SignalAvailability
import com.runmate.compose.health.BaselineResult
import com.runmate.compose.health.PersonalBaseline
import com.runmate.compose.health.BaselineDirection
import com.runmate.compose.health.NextAction
import com.runmate.compose.health.NextActionPolicy
import com.runmate.compose.health.ShapingFact
import com.runmate.compose.health.ShapingFactRanker
import com.runmate.compose.health.ShapingSignal
import com.runmate.compose.health.ShapingStrength
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Ink = Color(0xFF142A46)
private val Muted = Color(0xFF667A91)
private val Ocean = Color(0xFF197C9B)
private val CanvasColor = Color(0xFFF3F8FC)
private val Cyan = Color(0xFF9BE7F5)
private val Gold = Color(0xFFFFD26F)
private val Night = Color(0xFF071C31)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayDecisionScreen(viewModel: HealthDashboardViewModel?, onOpenHealth: () -> Unit = {}) {
    val state = viewModel?.state?.collectAsStateWithLifecycle()?.value
        ?: HealthDashboardUiState.Unavailable
    val content = when (state) {
        is HealthDashboardUiState.Content -> state.data
        is HealthDashboardUiState.Loading -> state.previous
        is HealthDashboardUiState.Error -> state.previous
        else -> null
    }
    val bodyPicture = when (state) {
        is HealthDashboardUiState.Content -> state.bodyPicture
        is HealthDashboardUiState.Loading -> state.previousBodyPicture
        is HealthDashboardUiState.Error -> state.previousBodyPicture
        else -> null
    }
    val refreshing = state is HealthDashboardUiState.Loading
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) {
        viewModel?.refresh()
    }
    val requestPermission = {
        val missing = (state as? HealthDashboardUiState.PermissionRequired)?.missing
        if (missing != null) permissionLauncher.launch(missing)
        else runCatching { context.startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)) }
        Unit
    }

    LaunchedEffect(viewModel) { viewModel?.refresh() }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { viewModel?.refresh() },
        modifier = Modifier.fillMaxSize().background(CanvasColor),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { TodayHeader(content != null) }
            item {
                HealthStatusHero(
                    state = state,
                    data = content,
                    bodyPicture = bodyPicture,
                )
            }
            item { TodayShapingCard(state, content, bodyPicture, onOpenHealth) }
            item {
                TodayNextCard(
                    state = state,
                    data = content,
                    bodyPicture = bodyPicture,
                    onOpenHealth = onOpenHealth,
                    onRefresh = { viewModel?.refresh() },
                    onPermission = requestPermission,
                )
            }
        }
    }
}

@Composable
private fun TodayHeader(connected: Boolean) {
    val todayLabel = remember {
        LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM", Locale.ENGLISH))
            .uppercase(Locale.ENGLISH)
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(todayLabel, color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Text("Today", color = Ink, fontSize = 32.sp, fontWeight = FontWeight.Black)
            Text(if (connected) "Health Connect records loaded" else "Waiting for measured health data", color = Muted, fontSize = 13.sp)
        }
        StatusBadge(if (connected) "MEASURED" else "NO DATA", connected)
    }
}

@Composable
private fun StatusBadge(text: String, positive: Boolean) {
    Box(
        Modifier.clip(RoundedCornerShape(99.dp))
            .background(if (positive) Color(0xFFDDF5E7) else Color(0xFFFFF1D6))
            .padding(horizontal = 10.dp, vertical = 7.dp),
    ) {
        Text(text, color = if (positive) Color(0xFF237347) else Color(0xFF8B650F), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun HealthStatusHero(
    state: HealthDashboardUiState,
    data: HealthDashboardData?,
    bodyPicture: BodyPictureModel?,
) {
    val title: String
    val detail: String
    val ready = data != null
    val bodySignals = bodyPicture?.signals.orEmpty()
    val availableSignals = bodySignals.filter { it.state == SignalAvailability.AVAILABLE }
    when (state) {
        is HealthDashboardUiState.Loading -> {
            title = if (ready) "Today's picture is updating" else "Reading today's signals"
            detail = if (ready) "Your previous records remain visible while Health Connect refreshes." else "Fetching the latest records from Health Connect."
        }
        HealthDashboardUiState.Unavailable -> {
            title = "Health Connect unavailable"
            detail = "Health records cannot be read on this device."
        }
        is HealthDashboardUiState.PermissionRequired -> {
            title = "Health access required"
            detail = "Allow access to read your measured health records."
        }
        is HealthDashboardUiState.Error -> {
            title = if (ready) "Your last picture is still available" else "Health data could not load"
            detail = if (ready) "Refresh failed; no newer conclusion is shown." else state.message
        }
        is HealthDashboardUiState.Content -> {
            title = when (availableSignals.size) {
                0 -> "Not enough evidence for today's picture"
                1 -> "${availableSignals.first().label} is available today"
                else -> "Your body picture is ready"
            }
            detail = ""
        }
    }

    Card(
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(10.dp),
    ) {
        Column(
            Modifier.background(Brush.linearGradient(listOf(Night, Color(0xFF0B4057), Color(0xFF147B89)))).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(.1f)), contentAlignment = Alignment.Center) {
                    if (state is HealthDashboardUiState.Loading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = Cyan, strokeWidth = 3.dp)
                    } else {
                        Icon(if (ready) Icons.Rounded.MonitorHeart else Icons.Rounded.Refresh, null, tint = if (ready) Cyan else Gold)
                    }
                }
                Column(Modifier.padding(start = 14.dp).weight(1f)) {
                    Text(if (ready) "HEALTH CONNECT  •  MEASURED" else "HEALTH CONNECT", color = if (ready) Cyan else Gold, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
            }
            if (detail.isNotBlank()) {
                Text(detail, color = Color.White.copy(.76f), fontSize = 13.sp, lineHeight = 19.sp)
            }
            if (data != null) {
                if (bodySignals.isNotEmpty()) BodySnapshotRow(bodySignals)
            }
        }
    }
}

@Composable
private fun BodySnapshotRow(signals: List<BodyPictureSignal>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        signals.take(3).forEach { signal ->
            SnapshotRing(
                label = signal.label.uppercase(Locale.ENGLISH),
                value = signal.displayValue(),
                progress = signal.ringProgress(),
                color = signalColor(signal.id),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun TodayShapingCard(
    state: HealthDashboardUiState,
    data: HealthDashboardData?,
    bodyPicture: BodyPictureModel?,
    onOpenHealth: () -> Unit,
) = SurfaceCard {
    val signals = bodyPicture?.signals.orEmpty().filter { it.state == SignalAvailability.AVAILABLE }
    val shapingFacts = data?.let { ShapingFactRanker.rank(it.facts, it.sevenDayTrend) }.orEmpty()
    Text("WHAT IS SHAPING TODAY", color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
    val title = when {
        signals.isEmpty() -> "No shaping factor can be identified yet"
        shapingFacts.isNotEmpty() -> shapingFacts.first().displayText()
        state is HealthDashboardUiState.Error -> "The last known evidence is still visible"
        state is HealthDashboardUiState.Loading -> "The last known evidence is being refreshed"
        else -> "Not enough baseline evidence to explain the estimate yet"
    }
    val detail = when {
        signals.isEmpty() && data.availableRecordCount() > 0 -> "Measured records exist, but the current policy has not selected enough evidence for an explanation."
        signals.isEmpty() -> "Missing evidence stays missing; no cause or pattern is inferred."
        shapingFacts.size > 1 -> shapingFacts[1].displayText()
        shapingFacts.size == 1 -> "No other strong or baseline-ready change stands out today."
        else -> "WholeMate will not infer a reason without current evidence and a personal baseline."
    }
    Text(title, color = Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 23.sp, modifier = Modifier.padding(top = 8.dp))
    Text(detail, color = Muted, fontSize = 13.sp, lineHeight = 19.sp, modifier = Modifier.padding(top = 6.dp))
    if (signals.isNotEmpty()) {
        Button(onClick = onOpenHealth, modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 4.dp)) {
            Text("View why")
        }
    }
}

@Composable
private fun TodayNextCard(
    state: HealthDashboardUiState,
    data: HealthDashboardData?,
    bodyPicture: BodyPictureModel?,
    onOpenHealth: () -> Unit,
    onRefresh: () -> Unit,
    onPermission: () -> Unit,
) = SurfaceCard {
    val signals = bodyPicture?.signals.orEmpty().filter { it.state == SignalAvailability.AVAILABLE }
    val shapingFacts = data?.let { ShapingFactRanker.rank(it.facts, it.sevenDayTrend) }.orEmpty()
    val policyAction = NextActionPolicy.select(
        shapingFacts = shapingFacts,
        bodyPicture = bodyPicture,
        healthAccessIncomplete = state is HealthDashboardUiState.PermissionRequired,
    )
    Text("WHAT NEXT", color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
    val instruction = when {
        policyAction == NextAction.COMPLETE_HEALTH_ACCESS -> "Complete health access so WholeMate can read the required evidence."
        state is HealthDashboardUiState.Error -> "Retry refresh before using this picture for a new decision."
        state is HealthDashboardUiState.Loading || policyAction == NextAction.REFRESH_TODAY -> "Refresh today's data while keeping the last known evidence visible."
        state == HealthDashboardUiState.Unavailable -> "Check Health Connect availability on this device."
        policyAction == NextAction.REVIEW_DETAILS -> "Review the evidence behind today's strongest change."
        else -> "Keep your usual plan and check back when new evidence arrives."
    }
    Text(instruction, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 22.sp, modifier = Modifier.padding(top = 8.dp))
    val actionLabel: String
    val action: () -> Unit
    when {
        state is HealthDashboardUiState.PermissionRequired -> { actionLabel = "Allow health access"; action = onPermission }
        state is HealthDashboardUiState.Error || state == HealthDashboardUiState.Unavailable -> { actionLabel = "Try again"; action = onRefresh }
        state !is HealthDashboardUiState.Loading -> { actionLabel = "See health trends"; action = onOpenHealth }
        else -> return@SurfaceCard
    }
    Button(onClick = action, modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 4.dp)) { Text(actionLabel) }
}

private fun HealthDashboardData?.availableRecordCount(): Int = this?.let {
    listOf(it.sleep, it.heartRate, it.restingHeartRate, it.hrv, it.respiratoryRate, it.latestActivity, it.stepsToday).count { record -> record != null }
} ?: 0

private fun ShapingFact.displayText(): String = when (signal) {
    ShapingSignal.SLEEPING_HEART_RATE -> when (direction) {
        BaselineDirection.CLOSE -> "Sleeping HR was close to your recent baseline."
        BaselineDirection.ABOVE -> "Sleeping HR was ${"%.1f".format(Locale.ENGLISH, kotlin.math.abs(deviationPercent))}% above your recent baseline."
        BaselineDirection.BELOW -> "Sleeping HR was ${"%.1f".format(Locale.ENGLISH, kotlin.math.abs(deviationPercent))}% below your recent baseline."
    }
    ShapingSignal.SLEEP_DURATION -> when (strength) {
        ShapingStrength.NEUTRAL -> "Sleep duration was close to your usual."
        ShapingStrength.STRONG -> {
            val minutes = kotlin.math.round(kotlin.math.abs(current - baseline) * 60.0).toInt()
            val directionText = if (direction == BaselineDirection.ABOVE) "longer" else "shorter"
            "Sleep duration was $minutes min $directionText than your recent baseline."
        }
    }
}

private fun BodyPictureSignal.displayValue(): String = value?.let { "$it${unit.orEmpty()}" } ?: "--"

private fun BodyPictureSignal.ringProgress(): Float? = when {
    state != SignalAvailability.AVAILABLE -> null
    id == BodyPictureSignalId.RECOVERY -> value?.toFloatOrNull()?.div(100f)?.coerceIn(0f, 1f)
    id == BodyPictureSignalId.SLEEP -> 1f
    else -> null
}

private fun signalColor(id: BodyPictureSignalId): Color = when (id) {
    BodyPictureSignalId.RECOVERY -> Color(0xFF75E6A4)
    BodyPictureSignalId.STRAIN, BodyPictureSignalId.MOVEMENT -> Gold
    BodyPictureSignalId.SLEEP -> Cyan
    BodyPictureSignalId.RESTING_HEART_RATE -> Color(0xFFFF9BAA)
    BodyPictureSignalId.STRESS -> Color(0xFFC7B8FF)
}

@Composable
private fun SnapshotRing(label: String, value: String, progress: Float?, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(86.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxSize()) {
                drawCircle(Color.White.copy(.12f), style = Stroke(width = 13f))
                progress?.let { drawArc(color, -90f, 300f * it.coerceIn(0f, 1f), false, style = Stroke(width = 13f, cap = StrokeCap.Round)) }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black, maxLines = 1)
                Text(label, color = Color.White.copy(.62f), fontSize = 7.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}

@Composable
private fun LatestSignalsCard(data: HealthDashboardData) = SurfaceCard {
    Text("LATEST RECORDS", color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
    Text("Direct from Health Connect", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    Text("Synced ${HealthDisplayFormatter.time(data.syncedAt)}", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
    Spacer(Modifier.height(14.dp))
    SignalRow(Icons.Rounded.Bedtime, "Sleep", HealthDisplayFormatter.sleep(data.sleep), data.sleep?.origin?.appLabel)
    Divider()
    SignalRow(Icons.Rounded.Favorite, "Heart rate", HealthDisplayFormatter.heartRate(data.heartRate), data.heartRate?.origin?.appLabel)
    Divider()
    SignalRow(Icons.Rounded.MonitorHeart, "HRV", HealthDisplayFormatter.hrv(data.hrv), data.hrv?.origin?.appLabel)
    Divider()
    SignalRow(Icons.Rounded.MonitorHeart, "Respiratory rate", HealthDisplayFormatter.respiratoryRate(data.respiratoryRate), data.respiratoryRate?.origin?.appLabel)
    Divider()
    SignalRow(Icons.Rounded.FitnessCenter, "Latest activity", HealthDisplayFormatter.activity(data.latestActivity), data.latestActivity?.origin?.appLabel)
    Divider()
    SignalRow(Icons.Rounded.FitnessCenter, "Steps", HealthDisplayFormatter.steps(data.stepsToday), "Health Connect aggregate")
}

@Composable
private fun Divider() = HorizontalDivider(Modifier.padding(vertical = 11.dp), color = Color(0xFFE5EDF3))

@Composable
private fun SignalRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String, source: String?) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFE1F4FA)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Ocean, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(label, color = Muted, fontSize = 11.sp)
            Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
            if (source != null) Text("Source: $source", color = Ocean, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
private fun PersonalBaselineCard(points: List<DailyHealthPoint>) = SurfaceCard {
    Text("PERSONAL BASELINE  •  CALCULATED", color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
    Text("Today vs your recent days", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    Text("Today is compared with at least 3 available days before today.", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))
    Spacer(Modifier.height(16.dp))
    BaselineRow("Sleep", PersonalBaseline.sleep(points), "hours", decimals = 1)
    Divider()
    BaselineRow("Average heart rate", PersonalBaseline.heartRate(points), "bpm", decimals = 0)
}

@Composable
private fun BaselineRow(label: String, result: BaselineResult, unit: String, decimals: Int) {
    Column(Modifier.fillMaxWidth()) {
        Text(label, color = Muted, fontSize = 11.sp)
        when (result) {
            is BaselineResult.Available -> {
                val comparison = result.comparison
                val sign = if (comparison.difference > 0) "+" else ""
                val delta = if (decimals == 0) comparison.difference.toInt().toString() else "%.1f".format(comparison.difference)
                val baseline = if (decimals == 0) comparison.baselineAverage.toInt().toString() else "%.1f".format(comparison.baselineAverage)
                Text("$sign$delta $unit vs baseline", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text("Baseline $baseline $unit from ${comparison.baselineSampleCount} previous days", color = Muted, fontSize = 10.sp)
            }
            is BaselineResult.InsufficientData -> {
                val reason = if (!result.currentAvailable) "No measured value for today" else "${result.baselineSampleCount}/${result.requiredBaselineSamples} baseline days available"
                Text("Not enough data", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
                Text(reason, color = Muted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun SevenDayChart(points: List<DailyHealthPoint>) {
    var metric by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableIntStateOf(6) }
    val haptic = LocalHapticFeedback.current
    val values = points.map { if (metric == 0) it.sleepHours else it.averageHeartRate }
    val chartValues = values.map { it ?: Double.NaN }
    val hasRecords = values.any { it != null }
    val labels = points.map { it.date.dayOfWeek.name.take(1) }

    SurfaceCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("7-DAY RECORDS", color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
                Text(if (metric == 0) "Sleep duration" else "Average heart rate", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
            Button(
                onClick = { metric = 1 - metric; selected = 6 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1F4FA), contentColor = Ocean),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
            ) { Text(if (metric == 0) "Show HR" else "Show sleep", fontSize = 11.sp) }
        }
        if (!hasRecords) {
            Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                Text("No measured records for this 7-day period", color = Muted, fontSize = 13.sp)
            }
            return@SurfaceCard
        }
        val chosen = chartValues.getOrNull(selected)
        Text(
            if (chosen == null || chosen.isNaN()) "No record for this day" else if (metric == 0) "${"%.1f".format(chosen)} hours" else "${chosen.toInt()} bpm",
            color = Ocean,
            fontSize = 22.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(top = 14.dp),
        )
        Box(
            Modifier.fillMaxWidth().height(150.dp).padding(top = 12.dp).pointerInput(chartValues) {
                detectDragGestures(
                    onDragStart = { offset ->
                        selected = ((offset.x / size.width) * 6).toInt().coerceIn(0, 6)
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    onDrag = { change, _ ->
                        val next = ((change.position.x / size.width) * 6).toInt().coerceIn(0, 6)
                        if (next != selected) {
                            selected = next
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                )
            },
        ) { SignalChart(chartValues, selected) }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEachIndexed { index, label ->
                Text(label, color = if (index == selected) Ocean else Muted, fontSize = 10.sp, fontWeight = if (index == selected) FontWeight.ExtraBold else FontWeight.Normal)
            }
        }
        Text("Measured from Health Connect", color = Muted, fontSize = 10.sp, modifier = Modifier.padding(top = 10.dp))
    }
}

@Composable
private fun SignalChart(values: List<Double>, selected: Int) {
    Canvas(Modifier.fillMaxSize()) {
        val finite = values.filter { !it.isNaN() }
        if (finite.isEmpty()) return@Canvas
        val min = finite.min() - 1.0
        val max = finite.max() + 1.0
        fun x(index: Int) = size.width * index / 6f
        fun y(value: Double) = size.height - (((value - min) / (max - min)) * size.height).toFloat()
        val path = Path()
        var started = false
        values.forEachIndexed { index, value ->
            if (!value.isNaN()) {
                if (!started) { path.moveTo(x(index), y(value)); started = true } else path.lineTo(x(index), y(value))
            }
        }
        drawPath(path, color = Ocean, style = Stroke(width = 6f, cap = StrokeCap.Round))
        values.forEachIndexed { index, value ->
            if (!value.isNaN()) drawCircle(if (index == selected) Gold else Ocean, radius = if (index == selected) 11f else 7f, center = androidx.compose.ui.geometry.Offset(x(index), y(value)))
        }
    }
}

@Composable
private fun SurfaceCard(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) { Column(Modifier.padding(18.dp), content = content) }
}
