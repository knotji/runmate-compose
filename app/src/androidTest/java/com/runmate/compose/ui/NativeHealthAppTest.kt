package com.runmate.compose.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NativeHealthAppTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun experimentIsDisabledByDefault() {
        compose.setContent { NativeHealthApp(experimentEnabled = false) }
        compose.onNodeWithText("RunMate Compose Lab").assertIsDisplayed()
        compose.onNodeWithText("Health Dashboard").assertDoesNotExist()
    }

    @Test
    fun enabledExperimentOpensTodayAndNavigatesToHealth() {
        compose.setContent { NativeHealthApp(experimentEnabled = true) }
        compose.onNodeWithText("Good morning").assertIsDisplayed()
        compose.onNodeWithText("Recovery").assertIsDisplayed()
        compose.onNodeWithText("Health").performClick()
        compose.onNodeWithText("Health Dashboard").assertIsDisplayed()
    }
}
