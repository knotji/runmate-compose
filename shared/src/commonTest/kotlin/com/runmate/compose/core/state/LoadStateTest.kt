package com.runmate.compose.core.state

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class LoadStateTest {
    @Test fun loadingKeepsPreviousContentVisible() = assertEquals("cached", LoadState.Loading("cached").visibleValue())
    @Test fun failureKeepsPreviousContentVisible() = assertEquals("cached", LoadState.Failed("offline", "cached").visibleValue())
    @Test fun emptyHasNoVisibleContent() = assertNull(LoadState.Empty("none").visibleValue())
}
