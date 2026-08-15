package com.runmate.compose.core.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LoadStateTest {
    @Test fun loadingKeepsPreviousContentVisible() {
        assertEquals("cached", LoadState.Loading("cached").visibleValue())
    }

    @Test fun failureKeepsPreviousContentVisible() {
        assertEquals("cached", LoadState.Failed("offline", "cached").visibleValue())
    }

    @Test fun emptyHasNoVisibleContent() {
        assertNull(LoadState.Empty("none").visibleValue())
    }
}
