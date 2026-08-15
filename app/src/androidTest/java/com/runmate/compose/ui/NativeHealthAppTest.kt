package com.runmate.compose.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test

class NativeHealthAppTest {
    @get:Rule val compose = createComposeRule()

    @Test
    fun experimentIsDisabledByDefault() {
        compose.setContent { NativeHealthApp(experimentEnabled = false) }
        compose.onNodeWithText("WholeMate").assertIsDisplayed()
        compose.onNodeWithText("Health Dashboard").assertDoesNotExist()
    }

    @Test
    fun enabledExperimentOpensTodayAndNavigatesToHealth() {
        compose.setContent { NativeHealthApp(experimentEnabled = true) }
        compose.onAllNodesWithText("Today").onFirst().assertIsDisplayed()
        compose.onNodeWithText("Health Connect unavailable").assertIsDisplayed()
        compose.onNodeWithText("WHAT IS SHAPING TODAY").assertIsDisplayed()
        compose.onNodeWithText("WHAT NEXT").assertIsDisplayed()
        compose.onNodeWithText("You").assertIsDisplayed()
        compose.onNodeWithText("Coach").assertDoesNotExist()
        compose.onNodeWithText("Health").performClick()
        compose.onNodeWithText("Health Dashboard").assertIsDisplayed()
    }

    @Test
    fun youDestinationDoesNotExposeCoachTopics() {
        compose.setContent { NativeHealthApp(experimentEnabled = true) }
        compose.onNodeWithText("You").performClick()
        compose.onNodeWithText("What Matters For You").assertIsDisplayed()
        compose.onNodeWithText("AI conversation").assertDoesNotExist()
    }
}
