package com.alexcawl.contract.benchmark.test

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.alexcawl.contract.benchmark.app.BenchmarkActivity
import com.alexcawl.contract.benchmark.app.BenchmarkScene
import org.junit.Rule
import org.junit.Test

class BenchmarkSceneTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<BenchmarkActivity>()

    @Test
    fun sceneDisplaysPlaceholder() {
        composeRule.setContent { BenchmarkScene() }
        composeRule.onNodeWithText("Appearance benchmark").assertIsDisplayed()
    }
}
