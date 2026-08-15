package com.runmate.compose.supabase

import java.net.HttpURLConnection
import java.net.URLEncoder
import java.net.URL
import java.time.Instant
import java.security.MessageDigest
import java.security.SecureRandom
import android.net.Uri
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject

data class WholeMateProfile(
    val displayName: String?,
    val mainGoal: String?,
    val secondaryGoal: String?,
    val timezone: String?,
    val language: String?,
    val updatedAt: String?,
)

sealed interface AuthResult {
    data class Success(val session: StoredSession) : AuthResult
    data class Failure(val message: String) : AuthResult
}

sealed interface ProfileResult {
    data class Success(val profile: WholeMateProfile?) : ProfileResult
    data class Failure(val message: String) : ProfileResult
}

class SupabaseAuthRepository(private val config: SupabaseConfig) {
    data class OAuthRequest(val url: String, val verifier: String)

    fun googleOAuthRequest(): OAuthRequest? {
        if (!config.isConfigured) return null
        val random = ByteArray(48).also(SecureRandom()::nextBytes)
        val verifier = Base64.encodeToString(random, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        val challenge = Base64.encodeToString(
            MessageDigest.getInstance("SHA-256").digest(verifier.toByteArray(Charsets.US_ASCII)),
            Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING,
        )
        val url = Uri.parse(config.url.trimEnd('/') + "/auth/v1/authorize").buildUpon()
            .appendQueryParameter("provider", "google")
            .appendQueryParameter("redirect_to", OAUTH_CALLBACK)
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("code_challenge_method", "s256")
            .build().toString()
        return OAuthRequest(url, verifier)
    }

    fun exchangeOAuthCode(code: String, verifier: String): AuthResult = authRequest(
        path = "/auth/v1/token?grant_type=pkce",
        body = JSONObject().put("auth_code", code).put("code_verifier", verifier),
    )
    fun signIn(email: String, password: String): AuthResult {
        if (!config.isConfigured) return AuthResult.Failure("Supabase is not configured")
        if (!email.contains('@') || password.isBlank()) return AuthResult.Failure("Enter a valid email and password")
        return authRequest(
            path = "/auth/v1/token?grant_type=password",
            body = JSONObject().put("email", email.trim()).put("password", password),
        )
    }

    fun refresh(refreshToken: String): AuthResult = authRequest(
        path = "/auth/v1/token?grant_type=refresh_token",
        body = JSONObject().put("refresh_token", refreshToken),
    )

    fun loadProfile(session: StoredSession): ProfileResult = runCatching {
        val id = URLEncoder.encode(session.userId, Charsets.UTF_8.name())
        val select = "display_name,main_goal,secondary_goal,timezone,language,updated_at"
        val response = request("GET", "/rest/v1/profiles?id=eq.$id&select=$select", session.accessToken)
        when (response.code) {
            in 200..299 -> {
                val rows = JSONArray(response.body)
                val row = if (rows.length() == 0) null else rows.getJSONObject(0)
                ProfileResult.Success(row?.let(::profileFromJson))
            }
            401 -> ProfileResult.Failure("Session expired")
            403 -> ProfileResult.Failure("Profile access was denied by RLS")
            else -> ProfileResult.Failure("Profile returned HTTP ${response.code}")
        }
    }.getOrElse { ProfileResult.Failure("Could not load profile") }

    fun signOut(accessToken: String) {
        runCatching { request("POST", "/auth/v1/logout", accessToken, JSONObject()) }
    }

    private fun authRequest(path: String, body: JSONObject): AuthResult = runCatching {
        val response = request("POST", path, body = body)
        if (response.code !in 200..299) {
            val message = runCatching { JSONObject(response.body).optString("msg") }.getOrNull()
            return@runCatching AuthResult.Failure(message?.takeIf(String::isNotBlank) ?: "Sign in failed")
        }
        val json = JSONObject(response.body)
        val user = json.getJSONObject("user")
        AuthResult.Success(
            StoredSession(
                accessToken = json.getString("access_token"),
                refreshToken = json.getString("refresh_token"),
                expiresAtEpochSeconds = Instant.now().epochSecond + json.optLong("expires_in", 3600),
                userId = user.getString("id"),
                email = user.optString("email"),
            ),
        )
    }.getOrElse { AuthResult.Failure("Could not reach Supabase Auth") }

    private data class Response(val code: Int, val body: String)

    private fun request(method: String, path: String, accessToken: String? = null, body: JSONObject? = null): Response {
        val connection = URL(config.url.trimEnd('/') + path).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("apikey", config.publishableKey)
            connection.setRequestProperty("Accept", "application/json")
            accessToken?.let { connection.setRequestProperty("Authorization", "Bearer $it") }
            body?.let {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json")
                connection.outputStream.use { output -> output.write(it.toString().toByteArray()) }
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            return Response(code, stream?.bufferedReader()?.use { it.readText() }.orEmpty())
        } finally {
            connection.disconnect()
        }
    }

    private fun profileFromJson(row: JSONObject) = WholeMateProfile(
        displayName = row.nullableString("display_name"),
        mainGoal = row.nullableString("main_goal"),
        secondaryGoal = row.nullableString("secondary_goal"),
        timezone = row.nullableString("timezone"),
        language = row.nullableString("language"),
        updatedAt = row.nullableString("updated_at"),
    )

    companion object { const val OAUTH_CALLBACK = "com.wholemate.app://auth/callback" }
}

private fun JSONObject.nullableString(key: String): String? =
    if (isNull(key)) null else optString(key).takeIf(String::isNotBlank)
