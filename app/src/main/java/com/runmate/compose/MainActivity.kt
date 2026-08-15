package com.runmate.compose

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.runmate.compose.health.HealthDashboardViewModel
import com.runmate.compose.supabase.SupabaseConnectionViewModel
import com.runmate.compose.state.RunMateAppStore
import com.runmate.compose.ui.NativeHealthApp

class MainActivity : ComponentActivity() {
    private val healthViewModel: HealthDashboardViewModel by viewModels()
    private val appStore: RunMateAppStore by viewModels()
    private val supabaseViewModel: SupabaseConnectionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        intent?.data?.let(supabaseViewModel::handleOAuthCallback)
        setContent {
            NativeHealthApp(BuildConfig.NATIVE_HEALTH_DASHBOARD, healthViewModel, appStore, supabaseViewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let(supabaseViewModel::handleOAuthCallback)
    }
}
