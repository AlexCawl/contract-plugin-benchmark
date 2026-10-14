package com.alexcawl.contract.benchmark.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card as MaterialCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.click
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.benchmark.library.common.CardState
import com.alexcawl.contract.benchmark.library.v1.Appearance
import com.alexcawl.contract.benchmark.library.v1.Card1
import com.alexcawl.contract.benchmark.library.v1.Size
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CardTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<BenchmarkActivity>()

    @Test
    fun arbitraryColumnContentReceivesPaddingSpacingAndMaterialContentColor() {
        var contentColor = Color.Unspecified
        val size = Size(contentPadding = { PaddingValues(10.dp) }, spacing = { 6.dp })
        val appearance = Appearance(colors = { CardDefaults.cardColors(contentColor = Color.Red) })
        composeRule.setContent {
            MaterialTheme {
                Card1(modifier = Modifier.width(200.dp).testTag("card"), size = size, appearance = appearance) {
                    val currentColor = LocalContentColor.current
                    SideEffect { contentColor = currentColor }
                    Box(Modifier.fillMaxWidth().height(20.dp).testTag("first"))
                    Box(Modifier.size(30.dp).align(Alignment.End).testTag("second"))
                }
            }
        }
        val card = composeRule.onNodeWithTag("card")
        card.assertHasNoClickAction().assertWidthIsEqualTo(200.dp).assertHeightIsEqualTo(76.dp)
        val cardBounds = card.getUnclippedBoundsInRoot()
        val first = composeRule.onNodeWithTag("first").getUnclippedBoundsInRoot()
        val second = composeRule.onNodeWithTag("second").getUnclippedBoundsInRoot()
        with(composeRule.density) {
            assertEquals(10.dp.roundToPx(), (first.left - cardBounds.left).roundToPx())
            assertEquals(10.dp.roundToPx(), (first.top - cardBounds.top).roundToPx())
            assertEquals(6.dp.roundToPx(), (second.top - first.bottom).roundToPx())
            assertEquals(10.dp.roundToPx(), (cardBounds.right - second.right).roundToPx())
            assertEquals(10.dp.roundToPx(), (cardBounds.bottom - second.bottom).roundToPx())
        }
        composeRule.runOnIdle { assertEquals(Color.Red, contentColor) }
    }

    @Test
    fun defaultsMatchNativeCardGeometryAndContentColor() {
        var nativeColor = Color.Unspecified
        var wrapperColor = Color.Unspecified
        composeRule.setContent {
            MaterialTheme {
                Column {
                    MaterialCard(modifier = Modifier.width(200.dp).testTag("native")) {
                        val color = LocalContentColor.current
                        SideEffect { nativeColor = color }
                        Box(Modifier.size(30.dp))
                    }
                    Card1(modifier = Modifier.width(200.dp).testTag("wrapper")) {
                        val color = LocalContentColor.current
                        SideEffect { wrapperColor = color }
                        Box(Modifier.size(30.dp))
                    }
                }
            }
        }
        val nativeBounds = composeRule.onNodeWithTag("native").getUnclippedBoundsInRoot()
        val wrapper = composeRule.onNodeWithTag("wrapper").assertHasNoClickAction()
        wrapper.assertWidthIsEqualTo(nativeBounds.right - nativeBounds.left)
            .assertHeightIsEqualTo(nativeBounds.bottom - nativeBounds.top)
        composeRule.runOnIdle { assertEquals(nativeColor, wrapperColor) }
    }

    @Test
    fun contractsControlContainerBorderAndClipping() {
        val rounded = mutableStateOf(false)
        val size = Size(shape = { if (rounded.value) RoundedCornerShape(16.dp) else RectangleShape })
        val appearance = Appearance(
            colors = { CardDefaults.cardColors(containerColor = Color.Yellow) },
            border = { BorderStroke(4.dp, Color.Red) },
        )
        composeRule.setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize().background(Color.Black)) {
                    Card1(modifier = Modifier.size(80.dp).testTag("card"), size = size, appearance = appearance) {}
                }
            }
        }
        val card = composeRule.onNodeWithTag("card")
        val rectangle = card.captureToImage().toPixelMap()
        assertEquals(Color.Yellow, rectangle[rectangle.width / 2, rectangle.height / 2])
        assertEquals(Color.Red, rectangle[1, rectangle.height / 2])
        composeRule.runOnIdle { rounded.value = true }
        val roundedImage = card.captureToImage().toPixelMap()
        assertEquals(Color.Black, roundedImage[0, 0])
        assertEquals(Color.Yellow, roundedImage[roundedImage.width / 2, roundedImage.height / 2])
    }

    @Test
    fun realPressReleaseCancelAndDisablingUpdateBothContracts() {
        val enabled = mutableStateOf(true)
        val source = MutableInteractionSource()
        var sizeState: CardState? = null
        var appearanceState: CardState? = null
        var clicks = 0
        val size = Size(
            contentPadding = { state ->
                SideEffect { sizeState = state }
                PaddingValues(if (state.pressed) 12.dp else 8.dp)
            },
        )
        val appearance = Appearance(
            colors = { state ->
                SideEffect { appearanceState = state }
                CardDefaults.cardColors()
            },
        )
        composeRule.setContent {
            MaterialTheme {
                Card1(
                    onClick = { clicks++ },
                    modifier = Modifier.width(160.dp).testTag("card"),
                    enabled = enabled.value,
                    size = size,
                    appearance = appearance,
                    interactionSource = source,
                ) {
                    Box(Modifier.size(40.dp))
                }
            }
        }
        val card = composeRule.onNodeWithTag("card")
        fun assertState(expected: CardState) {
            composeRule.runOnIdle {
                assertEquals(expected, sizeState)
                assertEquals(expected, appearanceState)
            }
        }

        card.assertHasClickAction().assertIsEnabled().assertHeightIsEqualTo(56.dp)
        assertState(CardState())
        card.performTouchInput { down(center) }
        assertState(CardState(pressed = true))
        card.assertHeightIsEqualTo(64.dp)
        card.performTouchInput { up() }
        assertState(CardState())
        card.assertHeightIsEqualTo(56.dp)
        composeRule.runOnIdle { assertEquals(1, clicks) }

        card.performTouchInput { down(center) }
        assertState(CardState(pressed = true))
        card.performTouchInput { cancel() }
        assertState(CardState())
        composeRule.runOnIdle { assertEquals(1, clicks) }

        card.performTouchInput { down(center) }
        assertState(CardState(pressed = true))
        composeRule.runOnIdle { enabled.value = false }
        assertState(CardState(enabled = false))
        card.assertIsNotEnabled().assertHeightIsEqualTo(56.dp)
        card.performTouchInput { up() }
        card.performTouchInput { click() }
        composeRule.runOnIdle {
            assertEquals(1, clicks)
            enabled.value = true
        }
        assertState(CardState())
        card.assertIsEnabled().performTouchInput { click() }
        composeRule.runOnIdle { assertEquals(2, clicks) }
    }

    @Test
    fun disabledCardUsesMaterialDisabledColors() {
        var contentColor = Color.Unspecified
        composeRule.setContent {
            MaterialTheme {
                Card1(
                    onClick = { error("Disabled card was clicked") },
                    modifier = Modifier.size(80.dp).testTag("card"),
                    enabled = false,
                    size = Size(shape = { RectangleShape }),
                    appearance = Appearance(
                        colors = {
                            CardDefaults.cardColors(
                                containerColor = Color.Red,
                                contentColor = Color.Green,
                                disabledContainerColor = Color.Gray,
                                disabledContentColor = Color.Blue,
                            )
                        },
                    ),
                ) {
                    val color = LocalContentColor.current
                    SideEffect { contentColor = color }
                }
            }
        }
        val card = composeRule.onNodeWithTag("card").assertIsNotEnabled()
        card.performTouchInput { click() }
        val pixels = card.captureToImage().toPixelMap()
        assertEquals(Color.Gray, pixels[pixels.width / 2, pixels.height / 2])
        composeRule.runOnIdle { assertEquals(Color.Blue, contentColor) }
    }
}
