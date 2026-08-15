package com.runmate.compose.supabase

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SupabaseConfigTest {
    @Test fun requiresHttpsUrlAndKey() {
        assertFalse(SupabaseConfig("", "").isConfigured)
        assertFalse(SupabaseConfig("http://example.supabase.co", "key").isConfigured)
        assertFalse(SupabaseConfig("https://example.supabase.co", "").isConfigured)
        assertTrue(SupabaseConfig("https://example.supabase.co", "sb_publishable_test").isConfigured)
    }

    @Test fun refreshesBeforeTokenActuallyExpires() {
        assertTrue(shouldRefreshSession(expiresAtEpochSeconds = 1_050, nowEpochSeconds = 1_000))
        assertTrue(shouldRefreshSession(expiresAtEpochSeconds = 1_060, nowEpochSeconds = 1_000))
        assertFalse(shouldRefreshSession(expiresAtEpochSeconds = 1_061, nowEpochSeconds = 1_000))
    }
}
