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
}
