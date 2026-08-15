package com.runmate.compose.supabase

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.runmate.compose.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SupabaseConfig(val url: String, val publishableKey: String) {
    val isConfigured: Boolean
        get() = url.startsWith("https://") && publishableKey.isNotBlank()
}

sealed interface SupabaseConnectionState {
    data object NotConfigured : SupabaseConnectionState
    data object Checking : SupabaseConnectionState
    data object Connected : SupabaseConnectionState
    data class Failed(val message: String) : SupabaseConnectionState
}

class SupabaseConnectionRepository(private val config: SupabaseConfig) {
    suspend fun check(): SupabaseConnectionState = withContext(Dispatchers.IO) {
        if (!config.isConfigured) return@withContext SupabaseConnectionState.NotConfigured
        runCatching {
            val endpoint = config.url.trimEnd('/') + "/rest/v1/"
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 5_000
                connection.readTimeout = 5_000
                connection.setRequestProperty("apikey", config.publishableKey)
                connection.setRequestProperty("Accept", "application/openapi+json")
                when (connection.responseCode) {
                    in 200..299 -> SupabaseConnectionState.Connected
                    401, 403 -> SupabaseConnectionState.Failed("Project key was rejected")
                    else -> SupabaseConnectionState.Failed("Project returned HTTP ${connection.responseCode}")
                }
            } finally {
                connection.disconnect()
            }
        }.getOrElse { SupabaseConnectionState.Failed("Could not reach the project") }
    }
}

class SupabaseConnectionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = SupabaseConnectionRepository(
        SupabaseConfig(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY),
    )
    private val mutableState = MutableStateFlow<SupabaseConnectionState>(SupabaseConnectionState.NotConfigured)
    val state: StateFlow<SupabaseConnectionState> = mutableState.asStateFlow()

    fun checkConnection() {
        if (mutableState.value == SupabaseConnectionState.Checking) return
        viewModelScope.launch {
            mutableState.value = SupabaseConnectionState.Checking
            mutableState.value = repository.check()
        }
    }
}
