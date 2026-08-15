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
import java.time.Instant

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
            val endpoint = config.url.trimEnd('/') + "/auth/v1/health"
            val connection = URL(endpoint).openConnection() as HttpURLConnection
            try {
                connection.requestMethod = "GET"
                connection.connectTimeout = 5_000
                connection.readTimeout = 5_000
                connection.setRequestProperty("apikey", config.publishableKey)
                connection.setRequestProperty("Accept", "application/json")
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
    private val config = SupabaseConfig(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_PUBLISHABLE_KEY)
    private val repository = SupabaseConnectionRepository(config)
    private val authRepository = SupabaseAuthRepository(config)
    private val sessionVault = SessionVault(application)
    private val mutableState = MutableStateFlow<SupabaseConnectionState>(SupabaseConnectionState.NotConfigured)
    val state: StateFlow<SupabaseConnectionState> = mutableState.asStateFlow()
    private val mutableAccountState = MutableStateFlow<AccountState>(AccountState.Restoring)
    val accountState: StateFlow<AccountState> = mutableAccountState.asStateFlow()

    init {
        restoreSession()
    }

    fun checkConnection() {
        if (mutableState.value == SupabaseConnectionState.Checking) return
        viewModelScope.launch {
            mutableState.value = SupabaseConnectionState.Checking
            mutableState.value = repository.check()
        }
    }

    fun signIn(email: String, password: String) {
        if (mutableAccountState.value is AccountState.Working) return
        viewModelScope.launch {
            mutableAccountState.value = AccountState.Working
            when (val result = withContext(Dispatchers.IO) { authRepository.signIn(email, password) }) {
                is AuthResult.Failure -> mutableAccountState.value = AccountState.SignedOut(result.message)
                is AuthResult.Success -> {
                    sessionVault.save(result.session)
                    loadProfile(result.session)
                }
            }
        }
    }

    fun signOut() {
        val session = (mutableAccountState.value as? AccountState.SignedIn)?.session
        sessionVault.clear()
        mutableAccountState.value = AccountState.SignedOut()
        if (session != null) viewModelScope.launch(Dispatchers.IO) { authRepository.signOut(session.accessToken) }
    }

    fun retryProfile() {
        val session = (mutableAccountState.value as? AccountState.SignedIn)?.session ?: sessionVault.load() ?: return
        viewModelScope.launch { loadProfile(session) }
    }

    private fun restoreSession() {
        viewModelScope.launch {
            val stored = withContext(Dispatchers.IO) { sessionVault.load() }
            if (stored == null) {
                mutableAccountState.value = AccountState.SignedOut()
                return@launch
            }
            val session = if (shouldRefreshSession(stored.expiresAtEpochSeconds, Instant.now().epochSecond)) {
                when (val refreshed = withContext(Dispatchers.IO) { authRepository.refresh(stored.refreshToken) }) {
                    is AuthResult.Success -> refreshed.session.also(sessionVault::save)
                    is AuthResult.Failure -> {
                        sessionVault.clear()
                        mutableAccountState.value = AccountState.SignedOut("Session expired. Sign in again.")
                        return@launch
                    }
                }
            } else stored
            loadProfile(session)
        }
    }

    private suspend fun loadProfile(session: StoredSession) {
        mutableAccountState.value = AccountState.Working
        mutableAccountState.value = when (val result = withContext(Dispatchers.IO) { authRepository.loadProfile(session) }) {
            is ProfileResult.Success -> AccountState.SignedIn(session, result.profile)
            is ProfileResult.Failure -> AccountState.SignedIn(session, null, result.message)
        }
    }
}

fun shouldRefreshSession(expiresAtEpochSeconds: Long, nowEpochSeconds: Long): Boolean =
    expiresAtEpochSeconds <= nowEpochSeconds + 60

sealed interface AccountState {
    data object Restoring : AccountState
    data object Working : AccountState
    data class SignedOut(val message: String? = null) : AccountState
    data class SignedIn(
        val session: StoredSession,
        val profile: WholeMateProfile?,
        val profileError: String? = null,
    ) : AccountState
}
