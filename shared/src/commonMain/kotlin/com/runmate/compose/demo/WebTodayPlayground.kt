package com.runmate.compose.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runmate.compose.core.state.LoadState
import com.runmate.compose.core.state.visibleValue
import com.runmate.compose.health.BodyPictureModel
import com.runmate.compose.health.BodyPictureSignal
import com.runmate.compose.health.DataCompleteness

private val Canvas = Color(0xFFF5F7F4)
private val SurfaceColor = Color(0xFFFFFFFF)
private val Ink = Color(0xFF17211D)
private val Muted = Color(0xFF68746E)
private val Line = Color(0xFFE2E8E3)
private val Moss = Color(0xFF276B50)
private val MossSoft = Color(0xFFE3F0E9)
private val AmberSoft = Color(0xFFFFF2D5)
private val DangerSoft = Color(0xFFFBE8E4)

private enum class LabDestination(val label: String, val marker: String) {
    TODAY("Today", "01"),
    HEALTH("Health", "02"),
    MOVE("Move", "03"),
    YOU("You", "04"),
}

@Composable
fun WebTodayPlayground(provider: DemoHealthProvider = remember { DemoHealthProvider() }) {
    var destination by remember { mutableStateOf(LabDestination.TODAY) }
    var scenario by remember { mutableStateOf(DemoTodayScenario.AVAILABLE) }
    val state = remember(scenario) { provider.load(scenario) }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Moss,
            background = Canvas,
            surface = SurfaceColor,
            onSurface = Ink,
        ),
    ) {
        Box(Modifier.fillMaxSize().background(Canvas), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 720.dp).fillMaxHeight().fillMaxWidth()) {
                DemoToolbar(scenario, onScenarioChange = { scenario = it })
                Column(
                    Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 22.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    when (destination) {
                        LabDestination.TODAY -> TodayPage(
                            state = state,
                            onOpenHealth = { destination = LabDestination.HEALTH },
                            onRetry = { scenario = DemoTodayScenario.AVAILABLE },
                        )
                        LabDestination.HEALTH -> HealthPage(scenario)
                        LabDestination.MOVE -> MovePage(scenario)
                        LabDestination.YOU -> YouPage(scenario)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Development playground only - deterministic demo data",
                        color = Muted,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                BottomNavigation(destination, onSelect = { destination = it })
            }
        }
    }
}

@Composable
private fun DemoToolbar(selected: DemoTodayScenario, onScenarioChange: (DemoTodayScenario) -> Unit) {
    Surface(color = Color(0xFFFFF8E9), shadowElevation = 1.dp) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("DESIGN LAB", color = Color(0xFF805E15), fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                Text("DEMO DATA - NOT HEALTH CONNECT", color = Color(0xFF805E15), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                DemoTodayScenario.entries.forEach { scenario ->
                    val active = scenario == selected
                    Button(
                        onClick = { onScenarioChange(scenario) },
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (active) Color(0xFF805E15) else Color.Transparent,
                            contentColor = if (active) Color.White else Color(0xFF805E15),
                        ),
                    ) { Text(scenario.shortLabel(), fontSize = 9.sp, fontWeight = FontWeight.Bold, maxLines = 1) }
                }
            }
        }
    }
}

@Composable
private fun PageHeader(eyebrow: String, title: String, summary: String) {
    Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
        Text(eyebrow, color = Moss, fontSize = 10.sp, fontWeight = FontWeight.Black)
        Text(title, color = Ink, fontSize = 30.sp, fontWeight = FontWeight.Black)
        Text(summary, color = Muted, fontSize = 14.sp, lineHeight = 20.sp)
    }
}

@Composable
private fun TodayPage(
    state: LoadState<BodyPictureModel>,
    onOpenHealth: () -> Unit,
    onRetry: () -> Unit,
) {
    val model = state.visibleValue()
    val usefulSignals = model?.signals.orEmpty().filter { it.value != null }
    PageHeader("TODAY - AUG 15", todayHeadline(state, model), todaySummary(state, model))

    SectionCard("BODY PICTURE", trailing = model?.completeness?.label()) {
        if (model == null) {
            LoadingLine("Building today's picture...")
        } else if (usefulSignals.isEmpty()) {
            Text("No measured signals are available for today's picture.", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("WholeMate will not fill missing evidence with scores or placeholders.", color = Muted, fontSize = 12.sp)
        } else {
            SignalRow(usefulSignals)
            Text(todayEvidenceStatus(state), color = Muted, fontSize = 10.sp)
        }
    }

    SectionCard("WHAT IS SHAPING TODAY") {
        Text(todayShapingTitle(state, usefulSignals), color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Text(todayShapingDetail(state, usefulSignals), color = Muted, fontSize = 13.sp)
        if (usefulSignals.isNotEmpty()) {
            ActionPill("Review sleep evidence", onClick = onOpenHealth)
        }
    }

    SectionCard("WHAT NEXT") {
        Text(todayNextStep(state, usefulSignals), color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        when {
            state is LoadState.Failed -> ActionPill("Retry demo refresh", onClick = onRetry)
            usefulSignals.isEmpty() -> ActionPill("Review data sources in Health", onClick = onOpenHealth)
        }
    }
}

@Composable
private fun HealthPage(scenario: DemoTodayScenario) {
    PageHeader("HEALTH", "Evidence over time", "See what is changing, where it came from, and how complete it is.")
    ScenarioGate(scenario) {
        SectionCard("NEEDS ATTENTION", trailing = "7 day view") {
            Text("Sleep timing has moved later on 3 of the last 5 nights.", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("Observed pattern - demo evidence", color = Muted, fontSize = 11.sp)
        }
        SectionCard("YOUR HEALTH") {
            EvidenceRow("Sleep", "7 h 24 m", "Near your baseline")
            EvidenceRow("Heart", "58 bpm", "Resting HR - measured")
            EvidenceRow("Breathing", if (scenario == DemoTodayScenario.PARTIAL) "No data" else "14.2 /min", "During sleep")
        }
        SectionCard("DATA QUALITY & SOURCES", trailing = "Review") {
            Text(if (scenario == DemoTodayScenario.PARTIAL) "Some evidence is missing" else "Demo evidence is complete", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text("Demo fixture - updated 08:15", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun MovePage(scenario: DemoTodayScenario) {
    PageHeader("MOVE", "How you are moving", "Everyday movement and activities, without treating everyone as a runner.")
    ScenarioGate(scenario) {
        SectionCard("TODAY") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MetricTile("6,420", "STEPS", Modifier.weight(1f))
                MetricTile("48 min", "ACTIVE", Modifier.weight(1f))
                MetricTile("3.9 km", "DISTANCE", Modifier.weight(1f))
            }
        }
        SectionCard("RECENT ACTIVITIES", trailing = "See history") {
            EvidenceRow("Evening walk", "32 min", "Yesterday - measured")
            EvidenceRow("Strength", "41 min", "Aug 12 - measured")
        }
        SectionCard("PATTERN") {
            Text("You moved on 5 of the last 7 days.", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text("A consistency fact, not a training prescription.", color = Muted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun YouPage(scenario: DemoTodayScenario) {
    PageHeader("YOU", "What matters for you", "Your goals and context help WholeMate make evidence personally useful.")
    SectionCard("CURRENT FOCUS", trailing = "Review") {
        Text("Sleep more consistently", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Text("A personal priority - it never changes measured records.", color = Muted, fontSize = 12.sp)
    }
    SectionCard("CONTEXT") {
        Text(if (scenario == DemoTodayScenario.MISSING) "No context shared today" else "No unusual stress or schedule change reported", color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        Text("Optional, private, and only used when relevant.", color = Muted, fontSize = 12.sp)
        ActionPill("Add context")
    }
    SectionCard("YOUR CONTROLS") {
        ControlRow("Goals", "One active focus")
        ControlRow("Data sources and access", "Demo provider")
        ControlRow("Privacy and account", "You stay in control")
    }
}

@Composable
private fun ScenarioGate(scenario: DemoTodayScenario, content: @Composable () -> Unit) {
    when (scenario) {
        DemoTodayScenario.LOADING -> StatusCard("Loading demo evidence", "The page shell stays stable while evidence loads.", MossSoft)
        DemoTodayScenario.ERROR -> StatusCard("Evidence is temporarily unavailable", "The demo provider could not refresh. Existing navigation remains usable.", DangerSoft, "Try again")
        DemoTodayScenario.MISSING -> StatusCard("No evidence for this view", "Nothing is inferred or filled with preview values.", AmberSoft, "Review data sources")
        else -> content()
    }
}

@Composable
private fun SectionCard(title: String, trailing: String? = null, content: @Composable () -> Unit) {
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = SurfaceColor), elevation = CardDefaults.cardElevation(0.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(Modifier.weight(1f))
                trailing?.let { Text(it, color = Moss, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            }
            content()
        }
    }
}

@Composable
private fun StatusCard(title: String, body: String, color: Color, action: String? = null) {
    Column(Modifier.fillMaxWidth().background(color, RoundedCornerShape(22.dp)).padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
        Text(title, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text(body, color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
        action?.let { ActionPill(it) }
    }
}

@Composable
private fun SignalRow(signals: List<BodyPictureSignal>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        signals.take(3).forEach { signal ->
            MetricTile(signal.value?.let { "$it ${signal.unit.orEmpty()}" } ?: "--", signal.label.uppercase(), Modifier.weight(1f))
        }
    }
}

@Composable
private fun MetricTile(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier.background(Canvas, RoundedCornerShape(16.dp)).padding(horizontal = 9.dp, vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Ink, fontSize = 16.sp, fontWeight = FontWeight.Black, maxLines = 1)
        Text(label, color = Muted, fontSize = 8.sp, fontWeight = FontWeight.Bold, maxLines = 1)
    }
}

@Composable
private fun EvidenceRow(label: String, value: String, detail: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).background(MossSoft, CircleShape), contentAlignment = Alignment.Center) {
            Text(label.take(1), color = Moss, fontWeight = FontWeight.Black)
        }
        Column(Modifier.weight(1f).padding(horizontal = 11.dp)) {
            Text(label, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(detail, color = Muted, fontSize = 11.sp)
        }
        Text(value, color = Ink, fontSize = 13.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ControlRow(label: String, detail: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(label, color = Ink, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Text(detail, color = Muted, fontSize = 11.sp)
        }
        Text(">", color = Moss, fontWeight = FontWeight.Black)
    }
}

@Composable
private fun ActionPill(label: String, onClick: (() -> Unit)? = null) {
    val actionModifier = if (onClick == null) Modifier else Modifier.clickable(onClick = onClick)
    Text(
        label,
        color = Moss,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = actionModifier.background(MossSoft, RoundedCornerShape(50)).padding(horizontal = 13.dp, vertical = 8.dp),
    )
}

@Composable
private fun LoadingLine(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        CircularProgressIndicator(Modifier.size(20.dp), color = Moss, strokeWidth = 2.dp)
        Text(label, color = Muted, fontSize = 13.sp)
    }
}

@Composable
private fun BottomNavigation(selected: LabDestination, onSelect: (LabDestination) -> Unit) {
    Surface(color = SurfaceColor, shadowElevation = 10.dp) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 9.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            LabDestination.entries.forEach { destination ->
                val active = destination == selected
                Column(
                    Modifier.weight(1f).clickable { onSelect(destination) }.padding(vertical = 7.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text(destination.marker, color = if (active) Moss else Muted, fontSize = 9.sp, fontWeight = FontWeight.Black)
                    Text(destination.label, color = if (active) Moss else Muted, fontSize = 11.sp, fontWeight = if (active) FontWeight.Black else FontWeight.Medium)
                    Box(Modifier.size(width = 18.dp, height = 2.dp).background(if (active) Moss else Color.Transparent, RoundedCornerShape(50)))
                }
            }
        }
    }
}

private fun DemoTodayScenario.shortLabel(): String = when (this) {
    DemoTodayScenario.AVAILABLE -> "Ready"
    DemoTodayScenario.PARTIAL -> "Partial"
    DemoTodayScenario.MISSING -> "Missing"
    DemoTodayScenario.LOADING -> "Loading"
    DemoTodayScenario.ERROR -> "Error"
}

private fun DataCompleteness.label(): String = when (this) {
    DataCompleteness.COMPLETE -> "Complete"
    DataCompleteness.PARTIAL -> "Partial"
    DataCompleteness.UNAVAILABLE -> "No evidence"
}

private fun todayHeadline(state: LoadState<BodyPictureModel>, model: BodyPictureModel?): String = when {
    state is LoadState.Failed -> "Your last known picture is incomplete"
    state is LoadState.Loading -> "Today's picture is still updating"
    model?.completeness == DataCompleteness.COMPLETE -> "Your body looks steady today"
    model?.signals?.any { it.value != null } == true -> "Today's picture is incomplete"
    else -> "Not enough evidence for today"
}

private fun todaySummary(state: LoadState<BodyPictureModel>, model: BodyPictureModel?): String = when {
    state is LoadState.Failed -> "The last available evidence remains visible, but the latest refresh failed."
    state is LoadState.Loading -> "Last available evidence stays visible while WholeMate refreshes."
    model?.completeness == DataCompleteness.COMPLETE -> "Sleep, resting heart rate, and movement show no strong warning signal."
    model?.signals?.any { it.value != null } == true -> "Sleep is available, but there is not enough evidence for a complete body picture."
    else -> "WholeMate cannot describe your state without measured evidence."
}

private fun todayEvidenceStatus(state: LoadState<BodyPictureModel>): String = when (state) {
    is LoadState.Failed -> "Last available evidence - refresh failed"
    is LoadState.Loading -> "Last available evidence - refreshing"
    else -> "Updated from deterministic demo evidence"
}

private fun todayShapingTitle(state: LoadState<BodyPictureModel>, usefulSignals: List<BodyPictureSignal>): String = when {
    usefulSignals.isEmpty() -> "No shaping factor can be identified yet."
    state is LoadState.Failed -> "Sleep was the clearest signal in the last available evidence."
    state is LoadState.Loading -> "Sleep is the clearest signal in the picture being refreshed."
    else -> "Sleep is the clearest signal in today's available evidence."
}

private fun todayShapingDetail(state: LoadState<BodyPictureModel>, usefulSignals: List<BodyPictureSignal>): String = when {
    usefulSignals.isEmpty() -> "Missing evidence stays missing; no cause or pattern is inferred."
    state is LoadState.Failed -> "No newer conclusion is shown until refresh succeeds."
    state is LoadState.Loading -> "No new conclusion is shown until refresh finishes."
    usefulSignals.size == 1 -> "Other body signals do not have enough data, so no recovery or strain conclusion is made."
    else -> "Other available signals are steady or do not have enough evidence to outweigh sleep."
}

private fun todayNextStep(state: LoadState<BodyPictureModel>, usefulSignals: List<BodyPictureSignal>): String = when {
    state is LoadState.Failed -> "Retry the demo refresh before using this picture for a new decision."
    state is LoadState.Loading -> "Wait for refresh to finish; keep using only the last available picture."
    usefulSignals.isEmpty() -> "Open Health and review data-source access before drawing a conclusion."
    usefulSignals.size == 1 -> "Keep your usual routine today and check again when more evidence arrives."
    else -> "Keep your regular wind-down time tonight."
}
