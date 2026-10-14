package com.alexcawl.contract.benchmark.test

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.alexcawl.contract.benchmark.app.BenchmarkActivity
import com.alexcawl.contract.benchmark.app.BenchmarkScene
import org.junit.Rule
import org.junit.Test

class BenchmarkSceneTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<BenchmarkActivity>()

    @Test
    fun sceneDisplaysExamplesAndTogglesSelection() {
        composeRule.setContent { BenchmarkScene() }
        composeRule.onNodeWithText("Standard action").assertIsDisplayed().assertIsNotSelected()
        composeRule.onNodeWithText("Selected action").assertIsDisplayed().assertIsSelected()
        composeRule.onNodeWithText("Unavailable action").assertIsDisplayed().assertIsNotEnabled()

        composeRule.onNodeWithText("Standard action").performClick().assertIsSelected()
        composeRule.onNodeWithText("Standard action").performClick().assertIsNotSelected()
    }
}
