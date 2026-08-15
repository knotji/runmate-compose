package com.runmate.compose.supabase

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class StoredSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSeconds: Long,
    val userId: String,
    val email: String,
)

class SessionVault(context: Context) {
    private val preferences = context.getSharedPreferences("wholemate_secure_session", Context.MODE_PRIVATE)

    fun save(session: StoredSession) {
        val payload = listOf(
            session.accessToken,
            session.refreshToken,
            session.expiresAtEpochSeconds.toString(),
            session.userId,
            session.email,
        ).joinToString("\u001f")
        preferences.edit().putString(SESSION_KEY, encrypt(payload)).apply()
    }

    fun load(): StoredSession? = runCatching {
        val encrypted = preferences.getString(SESSION_KEY, null) ?: return null
        val fields = decrypt(encrypted).split("\u001f")
        if (fields.size != 5) return null
        StoredSession(fields[0], fields[1], fields[2].toLong(), fields[3], fields[4])
    }.getOrNull()

    fun clear() {
        preferences.edit().remove(SESSION_KEY).apply()
    }

    private fun encrypt(plainText: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        val bytes = Base64.decode(value, Base64.NO_WRAP)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(128, bytes.copyOfRange(0, IV_BYTES)))
        return cipher.doFinal(bytes.copyOfRange(IV_BYTES, bytes.size)).toString(Charsets.UTF_8)
    }

    private fun key(): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return generator.generateKey()
    }

    private companion object {
        const val SESSION_KEY = "session"
        const val KEY_ALIAS = "wholemate_supabase_session_v1"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val IV_BYTES = 12
    }
}
