package com.runmate.compose.ui

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.rounded.AddCircle
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Insights
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Scale
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.MonitorHeart
import androidx.compose.material.icons.rounded.NightsStay
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.runmate.compose.health.HealthDashboardUiState
import com.runmate.compose.health.HealthDashboardViewModel
import com.runmate.compose.health.HealthDisplayFormatter
import com.runmate.compose.state.AppDestination
import com.runmate.compose.state.RunMateAppStore
import com.runmate.compose.supabase.SupabaseConnectionState
import com.runmate.compose.supabase.SupabaseConnectionViewModel
import com.runmate.compose.supabase.AccountState

private val Ink = Color(0xFF142A46)
private val Muted = Color(0xFF667A91)
private val Canvas = Color(0xFFF3F8FC)
private val Ocean = Color(0xFF197C9B)
private val Cyan = Color(0xFF9BE7F5)
private val Gold = Color(0xFFFFD26F)
private val Sky = Color(0xFFB7D8FF)
private val DarkBackground = Color(0xFF101A17)
private val DarkCard = Color(0xFF192823)
private val RunMateGreen = Color(0xFF75E6A4)

@Composable
fun NativeHealthApp(
    experimentEnabled: Boolean,
    viewModel: HealthDashboardViewModel? = null,
    appStore: RunMateAppStore? = null,
    supabaseViewModel: SupabaseConnectionViewModel? = null,
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Ocean,
            onPrimary = Color.White,
            background = Canvas,
            onBackground = Ink,
            surface = Color.White,
            onSurface = Ink,
            secondary = Color(0xFF4E7484),
        ),
    ) {
        if (!experimentEnabled) {
            ExperimentDisabled()
            return@MaterialTheme
        }

        if (supabaseViewModel != null) {
            val accountState by supabaseViewModel.accountState.collectAsStateWithLifecycle()
            if (accountState !is AccountState.SignedIn) {
                LoginScreen(accountState, supabaseViewModel)
                return@MaterialTheme
            }
        }

        val storedDestination = appStore?.destination?.collectAsStateWithLifecycle()?.value
        var previewTab by remember { mutableIntStateOf(0) }
        val selectedTab = storedDestination?.ordinal ?: previewTab
        Scaffold(
            containerColor = Canvas,
            bottomBar = {
                LabNavigation(selectedTab) { index ->
                    if (appStore == null) previewTab = index else appStore.navigate(AppDestination.entries[index])
                }
            },
        ) { padding ->
            Box(Modifier.padding(padding).fillMaxSize()) {
                when (selectedTab) {
                    0 -> TodayDecisionScreen(viewModel)
                    1 -> if (viewModel == null) HealthUnavailable() else NativeHealthDashboard(viewModel)
                    2 -> MoveScreen(viewModel)
                    else -> CoachScreen(supabaseViewModel)
                }
            }
        }
    }
}

@Composable
private fun LabNavigation(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        "Today" to Icons.Rounded.Home,
        "Health" to Icons.Rounded.Favorite,
        "Move" to Icons.AutoMirrored.Rounded.DirectionsWalk,
        "Coach" to Icons.Rounded.Psychology,
    )
    NavigationBar(containerColor = Color.White, tonalElevation = 10.dp) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                selected = selected == index,
                onClick = { onSelect(index) },
                icon = { Icon(item.second, contentDescription = item.first) },
                label = { Text(item.first, fontWeight = FontWeight.SemiBold) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    indicatorColor = Ocean,
                    selectedTextColor = Ocean,
                    unselectedIconColor = Muted,
                    unselectedTextColor = Muted,
                ),
            )
        }
    }
}

@Composable
private fun MoveScreen(viewModel: HealthDashboardViewModel?) {
    val state = viewModel?.state?.collectAsStateWithLifecycle()?.value
    LaunchedEffect(viewModel) { viewModel?.refresh() }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 24.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { ScreenHeading("MOVE", "Today’s Movement", "Activity is one signal inside your wider health picture.") }
        item { SectionHeading("TOOLS", "Plan And Review") }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CompactToolCard(Icons.Rounded.CalendarMonth, "Plan", "Not connected", Modifier.weight(1f))
                CompactToolCard(Icons.Rounded.Flag, "Goals", "Profile", Modifier.weight(1f))
                CompactToolCard(Icons.Rounded.Insights, "Summary", "Health", Modifier.weight(1f))
            }
        }
        item { SectionHeading("TODAY", "Measured Activity") }
        when (state) {
            is HealthDashboardUiState.Content -> {
                item { HubEntry(Icons.AutoMirrored.Rounded.DirectionsWalk, "Steps today", HealthDisplayFormatter.steps(state.data.stepsToday), if (state.data.stepsToday == null) "NO DATA" else "MEASURED") }
                item { HubEntry(Icons.AutoMirrored.Rounded.DirectionsRun, "Latest activity", HealthDisplayFormatter.activity(state.data.latestActivity), if (state.data.latestActivity == null) "NO DATA" else "HEALTH CONNECT") }
                item { Button(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Refresh, null); Text(" Refresh") } }
            }
            is HealthDashboardUiState.PermissionRequired -> item { DarkHealthCard("Health access needed", "Grant access from the Health tab to see movement.") }
            is HealthDashboardUiState.Error -> item { DarkHealthCard("Movement unavailable", state.message) }
            HealthDashboardUiState.Unavailable -> item { DarkHealthCard("Health Connect", "Unavailable on this device") }
            else -> item { LoadingState() }
        }
    }
}

@Composable
private fun CoachScreen(viewModel: SupabaseConnectionViewModel?) {
    val state = viewModel?.state?.collectAsStateWithLifecycle()?.value ?: SupabaseConnectionState.NotConfigured
    val accountState = viewModel?.accountState?.collectAsStateWithLifecycle()?.value ?: AccountState.SignedOut()
    LaunchedEffect(viewModel) { viewModel?.checkConnection() }
    val status = when (state) {
        SupabaseConnectionState.NotConfigured -> "Local project URL and publishable key are not configured"
        SupabaseConnectionState.Checking -> "Checking project connection…"
        SupabaseConnectionState.Connected -> "Project reachable • read/write remain disabled"
        is SupabaseConnectionState.Failed -> state.message
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 24.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { ScreenHeading("COACH", "Understand The Next Step", "Guidance will use your goals and measured health only when the evidence contract is ready.") }
        item { SectionHeading("CONTEXT", "Based On Your Data") }
        item { HubEntry(Icons.Rounded.Favorite, "Supabase", status, if (state == SupabaseConnectionState.Connected) "CONNECTED" else "CHECK") }
        when (val account = accountState) {
            AccountState.Restoring -> item { DarkHealthCard("Account", "Restoring encrypted session…") }
            AccountState.Working, AccountState.AwaitingGoogle -> item { LoadingState() }
            is AccountState.SignedOut -> {
                item { DarkHealthCard("Account", account.message ?: "Signed out") }
            }
            is AccountState.SignedIn -> {
                item { HubEntry(Icons.Rounded.Psychology, "Profile", account.profile?.displayName ?: account.session.email, "SIGNED IN") }
                item { HubEntry(Icons.Rounded.Flag, "Main goal", account.profile?.mainGoal ?: "No goal saved", if (account.profile?.mainGoal == null) "SETUP NEEDED" else "PROFILE") }
                account.profile?.secondaryGoal?.let { item { HubEntry(Icons.Rounded.Flag, "Secondary goal", it, "PROFILE") } }
                account.profileError?.let { item { DarkHealthCard("Profile unavailable", it) } }
                item { Button(onClick = { viewModel?.retryProfile() }, modifier = Modifier.fillMaxWidth()) { Text("Refresh profile") } }
                item { Button(onClick = { viewModel?.signOut() }, modifier = Modifier.fillMaxWidth()) { Text("Sign out") } }
            }
        }
        item { SectionHeading("TOPICS", "What Would You Like To Explore?") }
        item { HubEntry(Icons.Rounded.NightsStay, "Sleep and recovery", "Review measured sleep before future coaching.", "COMING LATER") }
        item { HubEntry(Icons.AutoMirrored.Rounded.DirectionsRun, "Movement and training", "Connect plans only after the shared contract is approved.", "COMING LATER") }
        item { HubEntry(Icons.Rounded.Restaurant, "Fuel and nutrition", "No nutrition records are connected in Compose yet.", "NOT CONNECTED") }
        item { HubEntry(Icons.Rounded.AutoAwesome, "AI conversation", "Unavailable until consent and evidence rules are approved.", "DISABLED") }
        item {
            Button(
                onClick = { viewModel?.checkConnection() },
                enabled = viewModel != null && state != SupabaseConnectionState.Checking,
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Check connection") }
        }
    }
}

@Composable
private fun ScreenHeading(label: String, title: String, detail: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Text(title, color = Ink, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold)
        Text(detail, color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
    }
}

@Composable
private fun SectionHeading(label: String, title: String) {
    Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, color = Ocean, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
        Text(title, color = Ink, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun HubEntry(icon: ImageVector, title: String, detail: String, status: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            IconBubble(icon, Color(0xFFE1F4FA), Ocean)
            Column(Modifier.padding(start = 13.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, color = Ink, fontWeight = FontWeight.Bold)
                Text(detail, color = Muted, fontSize = 12.sp, lineHeight = 17.sp)
                Text(status, color = Ocean, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .7.sp)
            }
        }
    }
}

@Composable
private fun CompactToolCard(icon: ImageVector, title: String, detail: String, modifier: Modifier = Modifier) {
    Card(modifier, colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(vertical = 14.dp, horizontal = 8.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Ocean)
            Text(title, color = Ink, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 6.dp))
            Text(detail, color = Muted, fontSize = 8.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun TodayMockup() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 20.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item { TodayHeader() }
        item { RecoveryHero() }
        item { TrainingCard() }
        item { EnergyCard() }
        item { CoachInsightCard() }
        item { Text("PREVIEW DATA • COMPOSE LAB", color = Muted, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) }
    }
}

@Composable
private fun TodayHeader() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("SATURDAY, 15 AUG", color = Ocean, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
            Text("Good morning", color = Ink, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            Text("Here’s how your body looks today.", color = Muted, fontSize = 14.sp)
        }
        IconButton(onClick = {}) { Icon(Icons.Rounded.Share, contentDescription = "Share", tint = Ocean) }
    }
}

@Composable
private fun RecoveryHero() {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(
            modifier = Modifier.background(
                Brush.linearGradient(listOf(Color(0xFF135D79), Color(0xFF1B829A), Color(0xFF269FAF))),
            ).padding(22.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("TODAY’S READINESS", color = Color.White.copy(alpha = .72f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text("Ready for quality work", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                }
                Box(Modifier.clip(RoundedCornerShape(99.dp)).background(Color.White.copy(alpha = .13f)).padding(horizontal = 10.dp, vertical = 6.dp)) {
                    Text("Preview", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                MetricDial("Recovery", 78, Cyan)
                MetricDial("Strain", 42, Gold)
                MetricDial("Sleep", 86, Sky)
            }
            Text(
                "Sleep supported your recovery. Keep today controlled and leave room for tomorrow.",
                color = Color.White.copy(alpha = .86f),
                fontSize = 13.sp,
                lineHeight = 19.sp,
            )
        }
    }
}

@Composable
private fun MetricDial(label: String, value: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(82.dp)) {
            CircularProgressIndicator(progress = { 1f }, modifier = Modifier.fillMaxSize(), color = Color.White.copy(alpha = .16f), strokeWidth = 7.dp)
            CircularProgressIndicator(progress = { value / 100f }, modifier = Modifier.fillMaxSize(), color = color, strokeWidth = 7.dp)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(value.toString(), color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
                Text("/100", color = Color.White.copy(alpha = .62f), fontSize = 9.sp)
            }
        }
        Text(label, color = Color.White.copy(alpha = .82f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun TrainingCard() = LightCard {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconBubble(Icons.AutoMirrored.Rounded.DirectionsRun, Color(0xFFE1F4FA), Ocean)
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text("TODAY’S TRAINING", color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
            Text("Easy aerobic run", color = Ink, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text("35–45 min • Conversational pace", color = Muted, fontSize = 13.sp)
        }
        Text("›", color = Ocean, fontSize = 28.sp)
    }
    Spacer(Modifier.height(14.dp))
    Text("Keep the effort smooth. If your legs feel heavy after 10 minutes, switch to a recovery walk.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
}

@Composable
private fun EnergyCard() = LightCard {
    Row(verticalAlignment = Alignment.CenterVertically) {
        IconBubble(Icons.Rounded.NightsStay, Color(0xFFFFF3D7), Color(0xFFB77A00))
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text("ENERGY RESERVE", color = Color(0xFF8B650F), fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
            Text("Steady through the afternoon", color = Ink, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        }
        Text("74%", color = Color(0xFFB77A00), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
    }
    Spacer(Modifier.height(14.dp))
    Box(Modifier.fillMaxWidth().height(9.dp).clip(CircleShape).background(Color(0xFFE8EEF3))) {
        Box(Modifier.fillMaxWidth(.74f).height(9.dp).clip(CircleShape).background(Brush.horizontalGradient(listOf(Gold, Color(0xFFFFB84D)))))
    }
}

@Composable
private fun CoachInsightCard() = LightCard(container = Color(0xFFEAF5FC)) {
    Row(verticalAlignment = Alignment.Top) {
        IconBubble(Icons.Rounded.Insights, Color.White, Ocean)
        Column(Modifier.padding(start = 14.dp).weight(1f)) {
            Text("RUNMATE INSIGHT", color = Ocean, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = .8.sp)
            Text("Your sleep timing was more consistent than your 7-day average.", color = Ink, fontSize = 15.sp, fontWeight = FontWeight.Bold, lineHeight = 21.sp)
            Text("That consistency is supporting today’s readiness.", color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun LightCard(container: Color = Color.White, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) { Column(Modifier.padding(18.dp), content = content) }
}

@Composable
private fun IconBubble(icon: ImageVector, background: Color, tint: Color) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(background), contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = null, tint = tint)
    }
}

@Composable
private fun ExperimentDisabled() {
    Surface(color = DarkBackground, modifier = Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center) {
            Text("WholeMate", color = Color.White, style = MaterialTheme.typography.headlineMedium)
            Text("Native experiment is disabled.", color = Color(0xFFB8C9C2), modifier = Modifier.padding(top = 12.dp))
        }
    }
}

@Composable
private fun NativeHealthDashboard(viewModel: HealthDashboardViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var permissionMessage by remember { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        permissionMessage = if (granted.isEmpty()) "No access was granted. Manage access in Health Connect settings." else "Health access updated."
        viewModel.refresh()
    }
    LaunchedEffect(Unit) { viewModel.refresh() }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp, 24.dp, 20.dp, 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item { ScreenHeading("YOUR HEALTH", "See What Is Shaping You", "Start with today’s measured signals, then review what is changing.") }
        item { Text("Compose experiment • read-only", color = RunMateGreen) }
        when (val current = state) {
            is HealthDashboardUiState.Loading -> item { LoadingState() }
            HealthDashboardUiState.Unavailable -> item { DarkHealthCard("Health Connect", "Unavailable on this device") }
            is HealthDashboardUiState.PermissionRequired -> {
                item { DarkHealthCard("Health Connect", "Permission is required to read real health data") }
                permissionMessage?.let { item { DarkHealthCard("Permission result", it) } }
                item { Button(onClick = { launcher.launch(current.missing) }, Modifier.fillMaxWidth()) { Text("Allow health access") } }
                item {
                    Button(onClick = { runCatching { context.startActivity(Intent(HealthConnectClient.ACTION_HEALTH_CONNECT_SETTINGS)) } }, modifier = Modifier.fillMaxWidth()) {
                        Text("Open Health Connect settings")
                    }
                }
            }
            is HealthDashboardUiState.Content -> {
                item { SectionHeading("OVERVIEW", "Your Body At A Glance") }
                item { HubEntry(Icons.Rounded.NightsStay, "Sleep", HealthDisplayFormatter.sleep(current.data.sleep), if (current.data.sleep == null) "NO DATA" else "MEASURED") }
                item { HubEntry(Icons.Rounded.Favorite, "Heart and HRV", "${HealthDisplayFormatter.heartRate(current.data.heartRate)} • ${HealthDisplayFormatter.hrv(current.data.hrv)}", "HEALTH CONNECT") }
                item { HubEntry(Icons.Rounded.MonitorHeart, "Respiratory rate", HealthDisplayFormatter.respiratoryRate(current.data.respiratoryRate), if (current.data.respiratoryRate == null) "NO DATA" else "MEASURED") }
                item { SectionHeading("TRENDS", "See What Is Changing") }
                item { HubEntry(Icons.Rounded.Insights, "Seven-day signals", "Sleep and heart history are available on the measured timeline.", "7 DAYS") }
                item { HubEntry(Icons.Rounded.Restaurant, "Nutrition", "No nutrition history is connected in Compose yet.", "NOT CONNECTED") }
                item { SectionHeading("BODY", "Longer-Term Signals") }
                item { HubEntry(Icons.Rounded.Scale, "Body weight", "Weight permission and trend are not enabled yet.", "NOT CONNECTED") }
                item { HubEntry(Icons.Rounded.Psychology, "Stress and mental wellbeing", "A consent-first input flow is not implemented yet.", "PLANNED") }
                item { SectionHeading("DATA SOURCES", "Connected Health Data") }
                item { HubEntry(Icons.Rounded.Favorite, "Health Connect", "Last synced ${HealthDisplayFormatter.time(current.data.syncedAt)}", "CONNECTED • READ ONLY") }
                item { Button(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth()) { Icon(Icons.Rounded.Refresh, null); Text(" Refresh") } }
            }
            is HealthDashboardUiState.Error -> {
                item { DarkHealthCard("Could not load health data", current.message) }
                item { Button(onClick = viewModel::refresh, modifier = Modifier.fillMaxWidth()) { Text("Retry") } }
            }
        }
    }
}

@Composable
private fun LoadingState() = Column(
    Modifier.fillMaxWidth().padding(vertical = 28.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(12.dp),
) {
    CircularProgressIndicator(color = Ocean)
    Text("Reading Health Connect…", color = Color(0xFFB8C9C2))
}

@Composable
private fun HealthUnavailable() = LazyColumn(
    modifier = Modifier.fillMaxSize(),
    contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
) {
    item { Text("Health Dashboard", color = Ink, fontSize = 27.sp, fontWeight = FontWeight.ExtraBold) }
    item { DarkHealthCard("Health Connect", "No data source is attached") }
}

@Composable
private fun DarkHealthCard(title: String, body: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, color = Ink, fontWeight = FontWeight.SemiBold)
            Text(body, color = Muted)
        }
    }
}

@Preview(showBackground = true, widthDp = 393, heightDp = 852)
@Composable
private fun TodayPreview() = NativeHealthApp(experimentEnabled = true)
