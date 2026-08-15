package com.runmate.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.runmate.compose.health.HealthDashboardViewModel
import com.runmate.compose.state.RunMateAppStore
import com.runmate.compose.ui.NativeHealthApp
import com.runmate.compose.today.TodayViewModel

class MainActivity : ComponentActivity() {
    private val healthViewModel: HealthDashboardViewModel by viewModels()
    private val appStore: RunMateAppStore by viewModels()
    private val todayViewModel: TodayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NativeHealthApp(BuildConfig.NATIVE_HEALTH_DASHBOARD, healthViewModel, appStore, todayViewModel)
        }
    }
}
