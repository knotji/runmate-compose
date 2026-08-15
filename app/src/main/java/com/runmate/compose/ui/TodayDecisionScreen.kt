package com.runmate.compose.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
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
import com.runmate.compose.health.DailyHealthPoint
import com.runmate.compose.health.HealthDashboardData
import com.runmate.compose.health.HealthDashboardUiState
import com.runmate.compose.health.HealthDashboardViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.format.TextStyle
import java.util.Locale

private val DecisionInk = Color(0xFF142A46)
private val DecisionMuted = Color(0xFF667A91)
private val DecisionOcean = Color(0xFF197C9B)
private val DecisionCanvas = Color(0xFFF3F8FC)
private val DecisionCyan = Color(0xFF9BE7F5)
private val DecisionGold = Color(0xFFFFD26F)
private val DecisionSky = Color(0xFFB7D8FF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodayDecisionScreen(viewModel: HealthDashboardViewModel?) {
    val state = viewModel?.state?.collectAsStateWithLifecycle()?.value
    val content = (state as? HealthDashboardUiState.Content)?.data
    val refreshing = state is HealthDashboardUiState.Loading
    var explanationOpen by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel) { viewModel?.refresh() }

    PullToRefreshBox(
        isRefreshing = refreshing,
        onRefresh = { viewModel?.refresh() },
        modifier = Modifier.fillMaxSize().background(DecisionCanvas),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(20.dp, 20.dp, 20.dp, 30.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { DecisionHeader(content != null) }
            item { DecisionRecoveryHero(onExplain = { explanationOpen = true }) }
            item { RealSignalsCard(content, refreshing) }
            item { SevenDayChart(content?.sevenDayTrend.orEmpty()) }
            item { InteractiveTrainingCard() }
            item { DecisionInsightCard(content) }
            item {
                Text(
                    "RECOVERY / STRAIN / ENERGY ARE PREVIEW • HEALTH SIGNALS ARE MEASURED",
                    color = DecisionMuted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }

    if (explanationOpen) {
        RecoveryExplanationSheet(onDismiss = { explanationOpen = false }, measured = content)
    }
}

@Composable
private fun DecisionHeader(connected: Boolean) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("TODAY • DECISION BUILD", color = DecisionOcean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Text("Good morning", color = DecisionInk, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text(if (connected) "Health Connect data is live" else "Pull down to read health signals", color = DecisionMuted, fontSize = 13.sp)
        }
        Box(
            Modifier.clip(RoundedCornerShape(99.dp)).background(if (connected) Color(0xFFDDF5E7) else Color(0xFFFFF1D6)).padding(horizontal = 10.dp, vertical = 7.dp),
        ) {
            Text(if (connected) "MEASURED" else "PREVIEW", color = if (connected) Color(0xFF237347) else Color(0xFF8B650F), fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun DecisionRecoveryHero(onExplain: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Card(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onExplain()
        },
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(12.dp),
    ) {
        Column(
            Modifier.background(Brush.linearGradient(listOf(Color(0xFF135D79), Color(0xFF1B829A), Color(0xFF269FAF)))).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("RUNMATE READINESS • PREVIEW", color = Color.White.copy(.7f), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Ready for quality work", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                Text("Tap to explain", color = Color.White.copy(.8f), fontSize = 11.sp)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                AnimatedDial("Recovery", 78, DecisionCyan)
                AnimatedDial("Strain", 42, DecisionGold)
                AnimatedDial("Sleep", 86, DecisionSky)
            }
            Text("Sleep supported your recovery. Keep today controlled and leave room for tomorrow.", color = Color.White.copy(.86f), fontSize = 13.sp, lineHeight = 19.sp)
        }
    }
}

@Composable
private fun AnimatedDial(label: String, value: Int, color: Color) {
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val progress by animateFloatAsState(if (started) value / 100f else 0f, tween(900), label = "$label progress")
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(82.dp), contentAlignment = Alignment.Center) {
            CircularProgressIndicator({ 1f }, Modifier.fillMaxSize(), color = Color.White.copy(.16f), strokeWidth = 7.dp)
            CircularProgressIndicator({ progress }, Modifier.fillMaxSize(), color = color, strokeWidth = 7.dp, strokeCap = StrokeCap.Round)
            Text(value.toString(), color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text(label, color = Color.White.copy(.82f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun RealSignalsCard(data: HealthDashboardData?, refreshing: Boolean) = DecisionCard {
    Row(verticalAlignment = Alignment.CenterVertically) {
        DecisionIcon(Icons.Rounded.Favorite, Color(0xFFE1F4FA), DecisionOcean)
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text("MEASURED SIGNALS", color = DecisionOcean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
            Text(if (refreshing) "Refreshing Health Connect…" else "Latest from Health Connect", color = DecisionInk, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
    Spacer(Modifier.height(14.dp))
    SignalRow("Sleep", data?.sleep ?: "No measured value loaded")
    HorizontalDivider(Modifier.padding(vertical = 10.dp), color = Color(0xFFE5EDF3))
    SignalRow("Heart rate", data?.heartRate ?: "No measured value loaded")
}

@Composable
private fun SignalRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, color = DecisionMuted, fontSize = 12.sp, modifier = Modifier.weight(.3f))
        Text(value, color = DecisionInk, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.End, modifier = Modifier.weight(.7f))
    }
}

@Composable
private fun SevenDayChart(points: List<DailyHealthPoint>) {
    var metric by rememberSaveable { mutableIntStateOf(0) }
    var selected by rememberSaveable { mutableIntStateOf(6) }
    val haptic = LocalHapticFeedback.current
    val values = points.map { if (metric == 0) it.sleepHours else it.averageHeartRate }
    val fallback = if (metric == 0) listOf(6.4, 7.1, 6.8, 7.5, 7.2, 8.0, 7.4) else listOf(67.0, 64.0, 66.0, 63.0, 61.0, 62.0, 60.0)
    val chartValues = if (values.any { it != null }) values.map { it ?: Double.NaN } else fallback
    val labels = if (points.size == 7) points.map { it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(1) } else listOf("S", "M", "T", "W", "T", "F", "S")
    val usingMeasured = values.any { it != null }

    DecisionCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("7-DAY SIGNAL", color = DecisionOcean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
                Text(if (metric == 0) "Sleep duration" else "Average heart rate", color = DecisionInk, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            }
            Button(
                onClick = { metric = 1 - metric; selected = 6 },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE1F4FA), contentColor = DecisionOcean),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 7.dp),
            ) { Text(if (metric == 0) "Show HR" else "Show sleep", fontSize = 11.sp) }
        }
        val chosen = chartValues.getOrNull(selected)
        Text(
            if (chosen == null || chosen.isNaN()) "No value for this day" else if (metric == 0) "${"%.1f".format(chosen)} hours" else "${chosen.toInt()} bpm",
            color = DecisionOcean,
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
        ) {
            SignalChart(chartValues, selected)
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEachIndexed { index, label -> Text(label, color = if (index == selected) DecisionOcean else DecisionMuted, fontSize = 10.sp, fontWeight = if (index == selected) FontWeight.ExtraBold else FontWeight.Normal) }
        }
        Text(if (usingMeasured) "Measured from Health Connect" else "Preview trend • no 7-day records loaded", color = DecisionMuted, fontSize = 10.sp, modifier = Modifier.padding(top = 10.dp))
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
        drawPath(path, color = DecisionOcean, style = Stroke(width = 6f, cap = StrokeCap.Round))
        values.forEachIndexed { index, value ->
            if (!value.isNaN()) drawCircle(if (index == selected) DecisionGold else DecisionOcean, radius = if (index == selected) 11f else 7f, center = androidx.compose.ui.geometry.Offset(x(index), y(value)))
        }
    }
}

@Composable
private fun InteractiveTrainingCard() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    var choice by rememberSaveable { mutableStateOf("Ready") }
    val haptic = LocalHapticFeedback.current
    Card(
        onClick = { expanded = !expanded; haptic.performHapticFeedback(HapticFeedbackType.LongPress) },
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
        DecisionIcon(Icons.AutoMirrored.Rounded.DirectionsRun, Color(0xFFE1F4FA), DecisionOcean)
                Column(Modifier.padding(start = 14.dp).weight(1f)) {
                    Text("TODAY’S TRAINING • PREVIEW", color = DecisionOcean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Easy aerobic run", color = DecisionInk, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text("35–45 min • Conversational pace", color = DecisionMuted, fontSize = 13.sp)
                }
                Icon(Icons.Rounded.ExpandMore, null, tint = DecisionOcean)
            }
            AnimatedVisibility(expanded) {
                Column(Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("How does this plan feel right now?", color = DecisionInk, fontWeight = FontWeight.Bold)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Ready", "Reduce", "Rest").forEach { option ->
                            Button(
                                onClick = { choice = option; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = if (choice == option) DecisionOcean else Color(0xFFE8F0F5), contentColor = if (choice == option) Color.White else DecisionMuted),
                                contentPadding = PaddingValues(horizontal = 4.dp),
                            ) { Text(option, fontSize = 11.sp) }
                        }
                    }
                    Text("Preview only • this does not change your production plan", color = DecisionMuted, fontSize = 10.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecoveryExplanationSheet(onDismiss: () -> Unit, measured: HealthDashboardData?) {
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 22.dp).padding(bottom = 34.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Why 78 today?", color = DecisionInk, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            Text("The score is a UI preview. The health evidence below is kept separate.", color = DecisionMuted, fontSize = 13.sp)
            ExplanationRow("MEASURED", "Sleep", measured?.sleep ?: "Not loaded", Color(0xFFDDF5E7))
            ExplanationRow("MEASURED", "Heart rate", measured?.heartRate ?: "Not loaded", Color(0xFFDDF5E7))
            ExplanationRow("DERIVED PREVIEW", "Sleep consistency", "+12 readiness", Color(0xFFFFF1D6))
            ExplanationRow("DERIVED PREVIEW", "Recent training load", "−6 readiness", Color(0xFFFFF1D6))
            Text("No HRV or respiratory value is fabricated when Health Connect has no record.", color = DecisionOcean, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ExplanationRow(badge: String, title: String, value: String, badgeColor: Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(Color(0xFFF5F8FA)).padding(14.dp)) {
        Text(badge, color = DecisionOcean, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.clip(CircleShape).background(badgeColor).padding(horizontal = 8.dp, vertical = 4.dp))
        Text(title, color = DecisionInk, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
        Text(value, color = DecisionMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 3.dp))
    }
}

@Composable
private fun DecisionInsightCard(data: HealthDashboardData?) = DecisionCard(container = Color(0xFFEAF5FC)) {
    Row(verticalAlignment = Alignment.Top) {
        DecisionIcon(Icons.Rounded.Insights, Color.White, DecisionOcean)
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text("RUNMATE INSIGHT • PREVIEW", color = DecisionOcean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
            Text(if (data == null) "Connect health data to ground this explanation." else "Your measured sleep is available to support a transparent readiness explanation.", color = DecisionInk, fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 21.sp)
        }
    }
}

@Composable
private fun DecisionCard(container: Color = Color.White, content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(container), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(18.dp), content = content)
    }
}

@Composable
private fun DecisionIcon(icon: androidx.compose.ui.graphics.vector.ImageVector, background: Color, tint: Color) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, null, tint = tint)
    }
}
