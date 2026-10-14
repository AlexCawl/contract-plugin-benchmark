package com.alexcawl.contract.benchmark.test

import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.benchmark.library.v1.Appearance1
import com.alexcawl.contract.benchmark.library.v1.Appearance1Presets
import com.alexcawl.contract.benchmark.library.v1.Size1
import com.alexcawl.contract.benchmark.library.v1.Size1Presets
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class V1PresetsTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<BenchmarkActivity>()

    @Test
    fun defaultsFollowMaterialThemeAndNativeColumnDefaults() {
        val changed = mutableStateOf(false)
        lateinit var appearance: Appearance1
        lateinit var size: Size1
        composeRule.setContent {
            MaterialTheme(
                colorScheme = if (changed.value) {
                    lightColorScheme(surfaceContainerHighest = Color.Blue, surfaceVariant = Color.Green)
                } else {
                    lightColorScheme(surfaceContainerHighest = Color.Red, surfaceVariant = Color.Yellow)
                },
                shapes = Shapes(medium = RoundedCornerShape(if (changed.value) 24.dp else 8.dp)),
            ) {
                val currentAppearance = Appearance1Presets.default
                val currentSize = Size1Presets.default
                val shape = currentAppearance.shape()
                val expectedShape = CardDefaults.shape
                val colors = listOf(
                    currentAppearance.containerColor(true),
                    currentAppearance.containerColor(false),
                    currentAppearance.contentColor(true),
                    currentAppearance.contentColor(false),
                )
                val expectedColors = CardDefaults.cardColors()
                val border = currentAppearance.border()
                val arrangement = currentSize.contentArrangement()
                val alignment = currentSize.contentAlignment()
                SideEffect {
                    appearance = currentAppearance
                    size = currentSize
                    assertEquals(expectedShape, shape)
                    assertEquals(
                        listOf(
                            expectedColors.containerColor,
                            expectedColors.disabledContainerColor,
                            expectedColors.contentColor,
                            expectedColors.disabledContentColor,
                        ),
                        colors,
                    )
                    assertNull(border)
                    assertEquals(Arrangement.Top, arrangement)
                    assertEquals(Alignment.Start, alignment)
                }
            }
        }
        composeRule.waitForIdle()
        val initialAppearance = appearance
        val initialSize = size
        composeRule.runOnIdle { changed.value = true }
        composeRule.runOnIdle {
            assertSame(initialAppearance, appearance)
            assertSame(initialSize, size)
        }
    }

    @Test
    fun elevationUsesLatestActiveInteractionAndHandlesEveryEndEvent() {
        val source = MutableInteractionSource()
        val enabled = mutableStateOf(true)
        var elevation = Dp.Unspecified
        composeRule.setContent {
            val value = Appearance1Presets.default.shadowElevation(enabled.value, source)
            SideEffect { elevation = value }
        }
        fun emit(interaction: Interaction, expected: Dp) {
            composeRule.runOnIdle { assertTrue(source.tryEmit(interaction)) }
            composeRule.mainClock.advanceTimeBy(300)
            composeRule.runOnIdle { assertEquals(expected, elevation) }
        }

        composeRule.runOnIdle { assertEquals(0.dp, elevation) }
        val hover = HoverInteraction.Enter()
        val press = PressInteraction.Press(Offset.Zero)
        val drag = DragInteraction.Start()
        val focus = FocusInteraction.Focus()
        emit(hover, 1.dp)
        emit(press, 0.dp)
        emit(drag, 6.dp)
        emit(PressInteraction.Release(press), 6.dp)
        emit(DragInteraction.Stop(drag), 1.dp)
        emit(focus, 0.dp)
        emit(FocusInteraction.Unfocus(focus), 1.dp)
        emit(HoverInteraction.Exit(hover), 0.dp)

        val cancelledDrag = DragInteraction.Start()
        val cancelledPress = PressInteraction.Press(Offset.Zero)
        emit(cancelledDrag, 6.dp)
        emit(DragInteraction.Cancel(cancelledDrag), 0.dp)
        emit(cancelledPress, 0.dp)
        emit(PressInteraction.Cancel(cancelledPress), 0.dp)

        val disabledHover = HoverInteraction.Enter()
        emit(disabledHover, 1.dp)
        composeRule.runOnIdle { enabled.value = false }
        composeRule.runOnIdle { assertEquals(0.dp, elevation) }
        composeRule.runOnIdle { enabled.value = true }
        composeRule.mainClock.advanceTimeBy(300)
        composeRule.runOnIdle { assertEquals(1.dp, elevation) }
        emit(HoverInteraction.Exit(disabledHover), 0.dp)
    }

    @Test
    fun elevationAnimatesButDisablingSnapsImmediately() {
        val source = MutableInteractionSource()
        val enabled = mutableStateOf(true)
        var elevation = Dp.Unspecified
        composeRule.setContent {
            val value = Appearance1Presets.default.shadowElevation(enabled.value, source)
            SideEffect { elevation = value }
        }
        composeRule.mainClock.autoAdvance = false
        composeRule.runOnIdle { assertTrue(source.tryEmit(HoverInteraction.Enter())) }
        composeRule.mainClock.advanceTimeBy(64)
        composeRule.runOnIdle { assertTrue(elevation > 0.dp && elevation < 1.dp) }
        composeRule.runOnIdle { enabled.value = false }
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.mainClock.advanceTimeByFrame()
        composeRule.runOnIdle { assertEquals(0.dp, elevation) }
    }
}
