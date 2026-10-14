package com.alexcawl.contract.benchmark.test

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.click
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import com.alexcawl.contract.benchmark.library.demo.CardDemoScene
import org.junit.Rule
import org.junit.Test

class CardDemoSceneTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<BenchmarkActivity>()

    @Test
    fun sceneDisplaysExamplesCountsClicksAndDisablesInteraction() {
        composeRule.setContent { MaterialTheme { CardDemoScene() } }
        composeRule.onNodeWithTag("default-card").assertIsDisplayed().assertHasNoClickAction()
        val interactive = composeRule.onNodeWithTag("interactive-card").assertIsDisplayed()
        val disabled = composeRule.onNodeWithTag("disabled-card").assertIsDisplayed().assertIsNotEnabled()

        interactive.performClick()
        composeRule.onNodeWithText("Клики: 1", useUnmergedTree = true).assertIsDisplayed()
        disabled.performTouchInput { click() }
        composeRule.onNodeWithText("Клики: 1", useUnmergedTree = true).assertIsDisplayed()
        composeRule.onNodeWithTag("enabled-toggle").performClick()
        interactive.assertIsNotEnabled().performTouchInput { click() }
        composeRule.onNodeWithText("Клики: 1", useUnmergedTree = true).assertIsDisplayed()
    }
}
