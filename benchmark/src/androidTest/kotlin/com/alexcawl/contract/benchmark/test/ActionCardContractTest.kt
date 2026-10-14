package com.alexcawl.contract.benchmark.test

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexcawl.contract.benchmark.app.BenchmarkActivity
import com.alexcawl.contract.benchmark.library.common.ActionCardState
import com.alexcawl.contract.benchmark.library.v1.ActionCardAppearance
import com.alexcawl.contract.benchmark.library.v1.ActionCardAppearanceWrapper
import com.alexcawl.contract.benchmark.library.v1.ActionCardSize
import com.alexcawl.contract.benchmark.library.v1.ActionCardSizeWrapper
import com.alexcawl.contract.benchmark.library.v1.copy
import com.alexcawl.contract.benchmark.library.v1.rememberCopy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

class ActionCardContractTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<BenchmarkActivity>()

    @Test
    fun factoryOverridesReceiveStateAndTheirReceiver() {
        val state = ActionCardState(enabled = false, selected = true)
        val shape = RoundedCornerShape(3.dp)
        val size = ActionCardSize(
            minHeight = { if (it.selected) 120.dp else 80.dp },
            contentPadding = { PaddingValues(3.dp) },
            horizontalSpacing = { 5.dp },
            verticalSpacing = { 7.dp },
            leadingSize = { trailingSize(it) * 2 },
            trailingSize = { 9.dp },
            badgePadding = { PaddingValues(11.dp) },
        )
        val appearance = ActionCardAppearance(
            shape = { shape },
            backgroundBrush = { if (it.selected) SolidColor(Color.Cyan) else null },
            borderStroke = { BorderStroke(3.dp, Color.Red) },
            titleTextStyle = { TextStyle(fontSize = 21.sp) },
            descriptionTextStyle = { TextStyle(fontSize = 17.sp) },
            badgeTextStyle = { TextStyle(fontSize = 13.sp) },
            badgeShape = { RectangleShape },
            badgeBackgroundBrush = { null },
            leadingColor = { if (it.enabled) Color.Green else Color.Gray },
            trailingColor = { leadingColor(it) },
        )
        composeRule.setContent {
            val sizeValues = size.values(state)
            val appearanceValues = appearance.values(state)
            SideEffect {
                assertEquals(
                    SizeValues(120.dp, PaddingValues(3.dp), 5.dp, 7.dp, 18.dp, 9.dp, PaddingValues(11.dp)),
                    sizeValues,
                )
                assertEquals(
                    AppearanceValues(
                        shape, SolidColor(Color.Cyan), BorderStroke(3.dp, Color.Red),
                        TextStyle(fontSize = 21.sp), TextStyle(fontSize = 17.sp), TextStyle(fontSize = 13.sp),
                        RectangleShape, null, Color.Gray, Color.Gray,
                    ),
                    appearanceValues,
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun defaultFactoriesMatchCompanionsForEveryState() {
        val size = ActionCardSize()
        val appearance = ActionCardAppearance()
        val states = listOf(
            ActionCardState(),
            ActionCardState(selected = true),
            ActionCardState(enabled = false),
            ActionCardState(enabled = false, selected = true),
        )
        composeRule.setContent {
            val actualSizes = states.map { size.values(it) }
            val expectedSizes = states.map { ActionCardSize.Default.values(it) }
            val actualAppearances = states.map { appearance.values(it) }
            val expectedAppearances = states.map { ActionCardAppearance.Default.values(it) }
            SideEffect {
                assertEquals(expectedSizes, actualSizes)
                assertEquals(expectedAppearances, actualAppearances)
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun copiesDelegateAndChainOverridesThroughWrapped() {
        val state = ActionCardState(selected = true)
        val size = ActionCardSize(minHeight = { if (it.selected) 100.dp else 80.dp })
        val sizeCopy = size.copy(minHeight = { wrapped.minHeight(it) + 8.dp })
        val sizeChain = sizeCopy.copy(horizontalSpacing = { wrapped.horizontalSpacing(it) + 1.dp })
        val appearance = ActionCardAppearance(leadingColor = { Color.Red })
        val appearanceCopy = appearance.copy(
            titleTextStyle = { wrapped.titleTextStyle(it).copy(fontSize = 24.sp) },
            borderStroke = { null },
        )
        val appearanceChain = appearanceCopy.copy(trailingColor = { wrapped.leadingColor(it) })
        val delegatedSize = size.copy()
        val delegatedAppearance = appearance.copy()

        assertSame(size, (sizeCopy as ActionCardSizeWrapper).wrapped)
        assertSame(sizeCopy, (sizeChain as ActionCardSizeWrapper).wrapped)
        assertSame(appearance, (appearanceCopy as ActionCardAppearanceWrapper).wrapped)
        assertSame(appearanceCopy, (appearanceChain as ActionCardAppearanceWrapper).wrapped)
        composeRule.setContent {
            val originalSize = size.values(state)
            val copiedSize = sizeChain.values(state)
            val unchangedSize = delegatedSize.values(state)
            val originalAppearance = appearance.values(state)
            val copiedAppearance = appearanceChain.values(state)
            val unchangedAppearance = delegatedAppearance.values(state)
            SideEffect {
                assertEquals(originalSize, unchangedSize)
                assertEquals(originalSize.copy(minHeight = 108.dp, horizontalSpacing = 13.dp), copiedSize)
                assertEquals(originalAppearance, unchangedAppearance)
                assertEquals(
                    originalAppearance.copy(
                        titleTextStyle = originalAppearance.titleTextStyle.copy(fontSize = 24.sp),
                        borderStroke = null,
                        trailingColor = Color.Red,
                    ),
                    copiedAppearance,
                )
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun rememberCopiesRetainFirstCapturesAndReceiverButReadChangingState() {
        val version = mutableStateOf(1)
        val color = mutableStateOf(Color.Green)
        val originalSize = ActionCardSize()
        val originalAppearance = ActionCardAppearance()
        val sizeReceiver = mutableStateOf(originalSize)
        val appearanceReceiver = mutableStateOf(originalAppearance)
        lateinit var rememberedSize: ActionCardSize
        lateinit var rememberedAppearance: ActionCardAppearance
        var height = Dp.Unspecified
        var spacing = Dp.Unspecified
        var leading = Color.Unspecified
        var trailing = Color.Unspecified
        composeRule.setContent {
            val capturedVersion = version.value
            val latestColor = rememberUpdatedState(color.value)
            val size = sizeReceiver.value.rememberCopy(
                minHeight = { capturedVersion.dp },
                horizontalSpacing = { version.value.dp },
            )
            val appearance = appearanceReceiver.value.rememberCopy(
                leadingColor = { if (capturedVersion == 1) Color.Red else Color.Blue },
                trailingColor = { latestColor.value },
            )
            val state = ActionCardState()
            val resolvedHeight = size.minHeight(state)
            val resolvedSpacing = size.horizontalSpacing(state)
            val resolvedLeading = appearance.leadingColor(state)
            val resolvedTrailing = appearance.trailingColor(state)
            SideEffect {
                rememberedSize = size
                rememberedAppearance = appearance
                height = resolvedHeight
                spacing = resolvedSpacing
                leading = resolvedLeading
                trailing = resolvedTrailing
            }
        }
        composeRule.waitForIdle()
        val firstSize = rememberedSize
        val firstAppearance = rememberedAppearance
        composeRule.runOnIdle {
            assertEquals(1.dp, height)
            assertEquals(1.dp, spacing)
            assertEquals(Color.Red, leading)
            assertEquals(Color.Green, trailing)
            version.value = 2
            color.value = Color.Blue
            sizeReceiver.value = ActionCardSize(minHeight = { 200.dp })
            appearanceReceiver.value = ActionCardAppearance(leadingColor = { Color.Black })
        }
        composeRule.runOnIdle {
            assertSame(firstSize, rememberedSize)
            assertSame(firstAppearance, rememberedAppearance)
            assertSame(originalSize, (rememberedSize as ActionCardSizeWrapper).wrapped)
            assertSame(originalAppearance, (rememberedAppearance as ActionCardAppearanceWrapper).wrapped)
            assertEquals(1.dp, height)
            assertEquals(2.dp, spacing)
            assertEquals(Color.Red, leading)
            assertEquals(Color.Blue, trailing)
        }
    }
}

private data class SizeValues(
    val minHeight: Dp,
    val contentPadding: PaddingValues,
    val horizontalSpacing: Dp,
    val verticalSpacing: Dp,
    val leadingSize: Dp,
    val trailingSize: Dp,
    val badgePadding: PaddingValues,
)

@Composable
private fun ActionCardSize.values(state: ActionCardState) = SizeValues(
    minHeight(state), contentPadding(state), horizontalSpacing(state), verticalSpacing(state),
    leadingSize(state), trailingSize(state), badgePadding(state),
)

private data class AppearanceValues(
    val shape: Shape,
    val backgroundBrush: Brush?,
    val borderStroke: BorderStroke?,
    val titleTextStyle: TextStyle,
    val descriptionTextStyle: TextStyle,
    val badgeTextStyle: TextStyle,
    val badgeShape: Shape,
    val badgeBackgroundBrush: Brush?,
    val leadingColor: Color,
    val trailingColor: Color,
)

@Composable
private fun ActionCardAppearance.values(state: ActionCardState) = AppearanceValues(
    shape(state), backgroundBrush(state), borderStroke(state), titleTextStyle(state),
    descriptionTextStyle(state), badgeTextStyle(state), badgeShape(state), badgeBackgroundBrush(state),
    leadingColor(state), trailingColor(state),
)
