package com.runmate.compose.ui

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
import com.runmate.compose.health.DailyHealthPoint
import com.runmate.compose.health.HealthDashboardData
import com.runmate.compose.health.HealthDashboardUiState
import com.runmate.compose.health.HealthDashboardViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
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
fun TodayDecisionScreen(viewModel: HealthDashboardViewModel?) {
    val state = viewModel?.state?.collectAsStateWithLifecycle()?.value
        ?: HealthDashboardUiState.Unavailable
    val content = (state as? HealthDashboardUiState.Content)?.data
    val refreshing = state is HealthDashboardUiState.Loading

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
            item { HealthStatusHero(state, onRefresh = { viewModel?.refresh() }) }
            if (content != null) {
                item { LatestSignalsCard(content) }
                item { SevenDayChart(content.sevenDayTrend) }
            }
            item {
                Text(
                    "ONLY RECORDS RETURNED BY HEALTH CONNECT ARE SHOWN",
                    color = Muted,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
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
private fun HealthStatusHero(state: HealthDashboardUiState, onRefresh: () -> Unit) {
    val title: String
    val detail: String
    val ready = state is HealthDashboardUiState.Content
    when (state) {
        HealthDashboardUiState.Loading -> {
            title = "Reading your signals"
            detail = "Fetching the latest records from Health Connect."
        }
        HealthDashboardUiState.Unavailable -> {
            title = "Health Connect unavailable"
            detail = "Health records cannot be read on this device."
        }
        is HealthDashboardUiState.PermissionRequired -> {
            title = "Health access required"
            detail = "Open the Health tab to grant access, then refresh Today."
        }
        is HealthDashboardUiState.Error -> {
            title = "Health data could not load"
            detail = state.message
        }
        is HealthDashboardUiState.Content -> {
            title = "Your measured signals"
            detail = "No recovery score or training recommendation is calculated in this build."
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
            Text(detail, color = Color.White.copy(.76f), fontSize = 13.sp, lineHeight = 19.sp)
            if (!ready && state !is HealthDashboardUiState.Loading) {
                Button(
                    onClick = onRefresh,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Night),
                ) { Text("Try again", fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun LatestSignalsCard(data: HealthDashboardData) = SurfaceCard {
    Text("LATEST RECORDS", color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
    Text("Direct from Health Connect", color = Ink, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    Spacer(Modifier.height(14.dp))
    SignalRow(Icons.Rounded.Bedtime, "Sleep", data.sleep)
    Divider()
    SignalRow(Icons.Rounded.Favorite, "Heart rate", data.heartRate)
    Divider()
    SignalRow(Icons.Rounded.MonitorHeart, "HRV", data.hrv)
    Divider()
    SignalRow(Icons.Rounded.MonitorHeart, "Respiratory rate", data.respiratoryRate)
    Divider()
    SignalRow(Icons.Rounded.FitnessCenter, "Workout", data.workout)
}

@Composable
private fun Divider() = HorizontalDivider(Modifier.padding(vertical = 11.dp), color = Color(0xFFE5EDF3))

@Composable
private fun SignalRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Box(Modifier.size(34.dp).clip(CircleShape).background(Color(0xFFE1F4FA)), contentAlignment = Alignment.Center) {
            Icon(icon, null, tint = Ocean, modifier = Modifier.size(18.dp))
        }
        Column(Modifier.padding(start = 12.dp).weight(1f)) {
            Text(label, color = Muted, fontSize = 11.sp)
            Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
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
    val labels = points.map { it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH).take(1) }

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
