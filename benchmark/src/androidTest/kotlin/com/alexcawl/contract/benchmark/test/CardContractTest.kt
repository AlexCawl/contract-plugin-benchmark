package com.alexcawl.contract.benchmark.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.benchmark.library.common.CardState
import com.alexcawl.contract.benchmark.library.v1.Appearance
import com.alexcawl.contract.benchmark.library.v1.Appearance1
import com.alexcawl.contract.benchmark.library.v1.Appearance1Wrapper
import com.alexcawl.contract.benchmark.library.v1.Size
import com.alexcawl.contract.benchmark.library.v1.Size1
import com.alexcawl.contract.benchmark.library.v1.Size1Wrapper
import com.alexcawl.contract.benchmark.library.v1.copy
import com.alexcawl.contract.benchmark.library.v1.rememberCopy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class CardContractTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<BenchmarkActivity>()

    @Test
    fun factoryOverridesReceiveStateAndTheirReceiver() {
        val state = CardState(enabled = false, pressed = true)
        val size = Size(
            shape = { RoundedCornerShape(if (it.pressed) spacing() else 1.dp) },
            contentPadding = { PaddingValues(if (it.enabled) 4.dp else 9.dp) },
            spacing = { 3.dp },
        )
        val appearance = Appearance(
            colors = {
                CardDefaults.cardColors(contentColor = if (it.enabled) Color.Green else Color.Gray)
            },
            border = { BorderStroke(2.dp, colors(it).contentColor) },
            elevation = { CardDefaults.cardElevation(defaultElevation = 2.dp) },
        )
        composeRule.setContent {
            MaterialTheme {
                val sizeValues = size.values(state)
                val appearanceValues = appearance.values(state)
                val expectedColors = CardDefaults.cardColors(contentColor = Color.Gray)
                val expectedElevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                SideEffect {
                    assertEquals(SizeValues(RoundedCornerShape(3.dp), PaddingValues(9.dp), 3.dp), sizeValues)
                    assertEquals(
                        AppearanceValues(expectedColors, BorderStroke(2.dp, Color.Gray), expectedElevation),
                        appearanceValues,
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun defaultFactoriesMatchCompanionsAndMaterialDefaultsForEveryState() {
        val size = Size()
        val appearance = Appearance()
        val states = listOf(
            CardState(),
            CardState(pressed = true),
            CardState(enabled = false),
            CardState(enabled = false, pressed = true),
        )
        composeRule.setContent {
            MaterialTheme {
                val actualSizes = states.map { size.values(it) }
                val companionSizes = states.map { Size1.Default.values(it) }
                val actualAppearances = states.map { appearance.values(it) }
                val companionAppearances = states.map { Appearance1.Default.values(it) }
                val expectedSize = SizeValues(CardDefaults.shape, PaddingValues(0.dp), 0.dp)
                val expectedAppearance = AppearanceValues(CardDefaults.cardColors(), null, CardDefaults.cardElevation())
                SideEffect {
                    assertEquals(List(states.size) { expectedSize }, actualSizes)
                    assertEquals(companionSizes, actualSizes)
                    assertEquals(List(states.size) { expectedAppearance }, actualAppearances)
                    assertEquals(companionAppearances, actualAppearances)
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun copiesDelegateAndChainOverridesThroughWrapped() {
        val state = CardState(pressed = true)
        val size = Size(spacing = { 6.dp })
        val sizeCopy = size.copy(contentPadding = { PaddingValues(wrapped.spacing() + 8.dp) })
        val sizeChain = sizeCopy.copy(spacing = { wrapped.spacing() + 1.dp })
        val appearance = Appearance(
            colors = { CardDefaults.cardColors(contentColor = Color.Red) },
            border = { BorderStroke(1.dp, Color.Black) },
        )
        val appearanceCopy = appearance.copy(
            colors = { wrapped.colors(it).copy(contentColor = Color.Yellow) },
            border = { null },
        )
        val appearanceChain = appearanceCopy.copy(
            border = { BorderStroke(2.dp, wrapped.colors(it).contentColor) },
        )
        val delegatedSize = size.copy()
        val delegatedAppearance = appearance.copy()

        assertSame(size, (sizeCopy as Size1Wrapper).wrapped)
        assertSame(sizeCopy, (sizeChain as Size1Wrapper).wrapped)
        assertSame(appearance, (appearanceCopy as Appearance1Wrapper).wrapped)
        assertSame(appearanceCopy, (appearanceChain as Appearance1Wrapper).wrapped)
        composeRule.setContent {
            MaterialTheme {
                val originalSize = size.values(state)
                val copiedSize = sizeChain.values(state)
                val unchangedSize = delegatedSize.values(state)
                val originalAppearance = appearance.values(state)
                val copiedAppearance = appearanceChain.values(state)
                val unchangedAppearance = delegatedAppearance.values(state)
                SideEffect {
                    assertEquals(originalSize, unchangedSize)
                    assertEquals(originalSize.copy(contentPadding = PaddingValues(14.dp), spacing = 7.dp), copiedSize)
                    assertEquals(originalAppearance, unchangedAppearance)
                    assertEquals(
                        originalAppearance.copy(
                            colors = originalAppearance.colors.copy(contentColor = Color.Yellow),
                            border = BorderStroke(2.dp, Color.Yellow),
                        ),
                        copiedAppearance,
                    )
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun rememberCopiesRetainFirstCapturesAndReceiverButReadChangingState() {
        val version = mutableStateOf(1)
        val color = mutableStateOf(Color.Green)
        val originalSize = Size()
        val originalAppearance = Appearance()
        val sizeReceiver = mutableStateOf(originalSize)
        val appearanceReceiver = mutableStateOf(originalAppearance)
        lateinit var rememberedSize: Size1
        lateinit var rememberedAppearance: Appearance1
        lateinit var padding: PaddingValues
        var spacing = Dp.Unspecified
        var borderColor = Color.Unspecified
        var contentColor = Color.Unspecified
        composeRule.setContent {
            MaterialTheme {
                val capturedVersion = version.value
                val latestColor = rememberUpdatedState(color.value)
                val size = sizeReceiver.value.rememberCopy(
                    contentPadding = { PaddingValues(capturedVersion.dp) },
                    spacing = { version.value.dp },
                )
                val appearance = appearanceReceiver.value.rememberCopy(
                    border = { BorderStroke(1.dp, if (capturedVersion == 1) Color.Red else Color.Blue) },
                    colors = { wrapped.colors(it).copy(contentColor = latestColor.value) },
                )
                val state = CardState()
                val resolvedPadding = size.contentPadding(state)
                val resolvedSpacing = size.spacing()
                val resolvedBorder = appearance.border(state)
                val resolvedColors = appearance.colors(state)
                SideEffect {
                    rememberedSize = size
                    rememberedAppearance = appearance
                    padding = resolvedPadding
                    spacing = resolvedSpacing
                    borderColor = (resolvedBorder!!.brush as SolidColor).value
                    contentColor = resolvedColors.contentColor
                }
            }
        }
        composeRule.waitForIdle()
        val firstSize = rememberedSize
        val firstAppearance = rememberedAppearance
        composeRule.runOnIdle {
            assertEquals(PaddingValues(1.dp), padding)
            assertEquals(1.dp, spacing)
            assertEquals(Color.Red, borderColor)
            assertEquals(Color.Green, contentColor)
            version.value = 2
            color.value = Color.Blue
            sizeReceiver.value = Size(spacing = { 200.dp })
            appearanceReceiver.value = Appearance(border = { null })
        }
        composeRule.runOnIdle {
            assertSame(firstSize, rememberedSize)
            assertSame(firstAppearance, rememberedAppearance)
            assertSame(originalSize, (rememberedSize as Size1Wrapper).wrapped)
            assertSame(originalAppearance, (rememberedAppearance as Appearance1Wrapper).wrapped)
            assertEquals(PaddingValues(1.dp), padding)
            assertEquals(2.dp, spacing)
            assertEquals(Color.Red, borderColor)
            assertEquals(Color.Blue, contentColor)
        }
    }

    @Test
    fun rememberedCopiesReadCurrentMaterialTheme() {
        val palette = mutableStateOf(lightColorScheme(surfaceContainerHighest = Color.Red))
        var actualColor = Color.Unspecified
        composeRule.setContent {
            MaterialTheme(colorScheme = palette.value) {
                val appearance = Appearance1.Default.rememberCopy()
                val colors = appearance.colors(CardState())
                SideEffect { actualColor = colors.containerColor }
            }
        }
        composeRule.runOnIdle {
            assertEquals(Color.Red, actualColor)
            palette.value = lightColorScheme(surfaceContainerHighest = Color.Blue)
        }
        composeRule.runOnIdle { assertEquals(Color.Blue, actualColor) }
    }
}

private data class SizeValues(val shape: Shape, val contentPadding: PaddingValues, val spacing: Dp)

@Composable
private fun Size1.values(state: CardState) = SizeValues(shape(state), contentPadding(state), spacing())

private data class AppearanceValues(val colors: CardColors, val border: BorderStroke?, val elevation: CardElevation)

@Composable
private fun Appearance1.values(state: CardState) = AppearanceValues(colors(state), border(state), shadowElevation())
