package com.runmate.compose.demo

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.runmate.compose.core.state.LoadState
import com.runmate.compose.core.state.visibleValue
import com.runmate.compose.health.BodyPictureModel
import com.runmate.compose.health.BodyPictureSignal
import com.runmate.compose.health.SignalAvailability

private val Canvas = Color(0xFFF3F8FC)
private val Ink = Color(0xFF142A46)
private val Muted = Color(0xFF667A91)
private val Ocean = Color(0xFF197C9B)
private val Night = Color(0xFF071C31)

@Composable
fun WebTodayPlayground(provider: DemoHealthProvider = remember { DemoHealthProvider() }) {
    var scenario by remember { mutableStateOf(DemoTodayScenario.AVAILABLE) }
    val state = remember(scenario) { provider.load(scenario) }
    MaterialTheme(lightColorScheme(primary = Ocean, background = Canvas, surface = Color.White, onSurface = Ink)) {
        Box(Modifier.fillMaxSize().background(Canvas), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 760.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                DemoBanner()
                Text("TODAY", color = Ocean, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                Text("How are you today?", color = Ink, fontSize = 32.sp, fontWeight = FontWeight.Black)
                Text("Switch deterministic fixtures to iterate the shared Today UI.", color = Muted, fontSize = 14.sp)
                ScenarioSelector(scenario) { scenario = it }
                TodayHero(state)
                Text("Development playground only • no authentication • no real health provider", color = Muted, fontSize = 11.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
private fun DemoBanner() = Box(
    Modifier.fillMaxWidth().background(Color(0xFFFFF1D6), RoundedCornerShape(14.dp)).padding(12.dp),
) {
    Text("DEMO DATA — NOT HEALTH CONNECT", color = Color(0xFF8B650F), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
}

@Composable
private fun ScenarioSelector(selected: DemoTodayScenario, onSelect: (DemoTodayScenario) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DemoTodayScenario.entries.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { scenario ->
                    Button(
                        onClick = { onSelect(scenario) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (scenario == selected) Ocean else Color.White,
                            contentColor = if (scenario == selected) Color.White else Ink,
                        ),
                    ) { Text(scenario.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
                }
            }
        }
    }
}

@Composable
private fun TodayHero(state: LoadState<BodyPictureModel>) {
    val model = state.visibleValue()
    Card(shape = RoundedCornerShape(30.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
        Column(
            Modifier.fillMaxWidth().background(Brush.linearGradient(listOf(Night, Color(0xFF0B4057), Color(0xFF147B89)))).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("WHOLEMATE • DEMO EVIDENCE", color = Color(0xFF9BE7F5), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                    Text(model?.headline ?: "Today's body picture", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                }
                if (state is LoadState.Loading) CircularProgressIndicator(color = Color(0xFF9BE7F5))
            }
            when (state) {
                is LoadState.Failed -> Text(state.message, color = Color(0xFFFFD26F), fontWeight = FontWeight.Bold)
                is LoadState.Loading -> Text("Loading demo health facts… previous content stays visible.", color = Color.White.copy(.72f))
                else -> Unit
            }
            if (model != null) SignalRow(model.signals)
            if (model?.signals?.all { it.value == null } == true) {
                Text("No demo measurements are available for this state.", color = Color.White.copy(.72f))
            }
            Text("Deterministic shared model • AI interpretation is off", color = Color.White.copy(.58f), fontSize = 10.sp)
        }
    }
}

@Composable
private fun SignalRow(signals: List<BodyPictureSignal>) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        signals.forEach { signal ->
            Card(Modifier.weight(1f), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White.copy(.10f))) {
                Column(Modifier.fillMaxWidth().padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(signal.value?.let { "$it ${signal.unit.orEmpty()}" } ?: "--", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black, maxLines = 1)
                    Spacer(Modifier.height(5.dp))
                    Text(signal.label.uppercase(), color = Color.White.copy(.66f), fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
                    Text(signal.state.label(), color = Color.White.copy(.58f), fontSize = 7.sp, maxLines = 1)
                }
            }
        }
    }
}

private fun SignalAvailability.label(): String = when (this) {
    SignalAvailability.AVAILABLE -> "AVAILABLE"
    SignalAvailability.MISSING -> "NO DATA"
    SignalAvailability.NOT_PERMITTED -> "NOT PERMITTED"
    SignalAvailability.NOT_SUPPORTED -> "NOT SUPPORTED"
    SignalAvailability.NOT_CONNECTED -> "NOT CONNECTED"
    SignalAvailability.STALE -> "STALE"
    SignalAvailability.INSUFFICIENT_DATA -> "INSUFFICIENT DATA"
}
