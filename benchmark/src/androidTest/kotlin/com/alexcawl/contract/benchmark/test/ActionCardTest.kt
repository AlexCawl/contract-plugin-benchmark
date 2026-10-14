package com.alexcawl.contract.benchmark.test

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertHeightIsEqualTo
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertWidthIsEqualTo
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexcawl.contract.benchmark.app.BenchmarkActivity
import com.alexcawl.contract.benchmark.app.common.ActionCard as CommonActionCard
import com.alexcawl.contract.benchmark.app.v1.ActionCard
import com.alexcawl.contract.benchmark.library.common.ActionCardState
import com.alexcawl.contract.benchmark.library.v1.ActionCardAppearance
import com.alexcawl.contract.benchmark.library.v1.ActionCardSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ActionCardTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<BenchmarkActivity>()

    @Test
    fun displaysOptionalContentAndPassesSlotColors() {
        var leadingColor = Color.Unspecified
        var trailingColor = Color.Unspecified
        val appearance = ActionCardAppearance(
            leadingColor = { Color.Red },
            trailingColor = { Color.Blue },
        )
        composeRule.setContent {
            ActionCard(
                title = "Title",
                description = "Description",
                badge = "Badge",
                onClick = {},
                modifier = Modifier.width(300.dp),
                appearance = appearance,
                leading = { color ->
                    SideEffect { leadingColor = color }
                    BasicText("L", style = TextStyle(color = color))
                },
                trailing = { color ->
                    SideEffect { trailingColor = color }
                    BasicText("R", style = TextStyle(color = color))
                },
            )
        }

        listOf("Title", "Description", "Badge", "L", "R").forEach {
            composeRule.onNodeWithText(it, useUnmergedTree = true).assertIsDisplayed()
        }
        composeRule.runOnIdle {
            assertEquals(Color.Red, leadingColor)
            assertEquals(Color.Blue, trailingColor)
        }
    }

    @Test
    fun absentOptionalContentReservesNoSpace() {
        val title = "Title only ".repeat(40)
        val size = ActionCardSize(
            minHeight = { 0.dp },
            contentPadding = { PaddingValues(0.dp) },
            horizontalSpacing = { 20.dp },
            verticalSpacing = { 20.dp },
        )
        composeRule.setContent {
            ActionCard(
                title = title,
                onClick = {},
                modifier = Modifier.width(280.dp).testTag("card"),
                size = size,
            )
        }

        val cardBounds = composeRule.onNodeWithTag("card").getUnclippedBoundsInRoot()
        val titleBounds = composeRule.onNodeWithText(title, useUnmergedTree = true)
            .getUnclippedBoundsInRoot()
        assertEquals(cardBounds, titleBounds)
    }

    @Test
    fun commonRendererAppliesExplicitGeometryAndTypography() {
        val titleStyle = TextStyle(color = Color.Red, fontSize = 20.sp)
        composeRule.setContent {
            CommonActionCard(
                title = "Explicit title",
                description = null,
                badge = null,
                state = ActionCardState(),
                onClick = {},
                modifier = Modifier.width(280.dp).testTag("card"),
                minHeight = 120.dp,
                contentPadding = PaddingValues(10.dp),
                horizontalSpacing = 8.dp,
                verticalSpacing = 6.dp,
                leadingSize = 30.dp,
                trailingSize = 20.dp,
                badgePadding = PaddingValues(2.dp),
                shape = RectangleShape,
                backgroundBrush = SolidColor(Color.Yellow),
                borderStroke = null,
                titleTextStyle = titleStyle,
                descriptionTextStyle = TextStyle(fontSize = 14.sp),
                badgeTextStyle = TextStyle(fontSize = 10.sp),
                badgeShape = RectangleShape,
                badgeBackgroundBrush = null,
                leadingColor = Color.Red,
                trailingColor = Color.Blue,
                leading = { color ->
                    BasicText(
                        "L",
                        modifier = Modifier.fillMaxSize().testTag("leading"),
                        style = TextStyle(color = color),
                    )
                },
                trailing = null,
            )
        }
        composeRule.onNodeWithTag("card").assertHeightIsEqualTo(120.dp)
        composeRule.onNodeWithTag("leading", useUnmergedTree = true)
            .assertWidthIsEqualTo(30.dp).assertHeightIsEqualTo(30.dp)
        val title = composeRule.onNodeWithText("Explicit title", useUnmergedTree = true)
        val cardBounds = composeRule.onNodeWithTag("card").getUnclippedBoundsInRoot()
        assertEquals(cardBounds.left + 48.dp, title.getUnclippedBoundsInRoot().left)
        val results = mutableListOf<TextLayoutResult>()
        title.performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
        assertEquals(titleStyle, results.single().layoutInput.style)
    }

    @Test
    fun longTextIsEllipsizedWithinItsLineLimit() {
        val title = "Long title ".repeat(30)
        val description = "Long description ".repeat(30)
        val badge = "Long badge ".repeat(30)
        composeRule.setContent {
            ActionCard(
                title = title,
                description = description,
                badge = badge,
                onClick = {},
                modifier = Modifier.width(180.dp),
            )
        }
        listOf(title to 1, description to 2, badge to 1).forEach { (text, maxLines) ->
            val results = mutableListOf<TextLayoutResult>()
            composeRule.onNodeWithText(text, useUnmergedTree = true)
                .performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(results) }
            assertEquals(maxLines, results.single().lineCount)
            assertTrue(results.single().isLineEllipsized(maxLines - 1))
        }
    }

    @Test
    fun stateTransitionsUpdateContractsSemanticsAndClicks() {
        val state = mutableStateOf(ActionCardState())
        var clicks = 0
        var sizeState: ActionCardState? = null
        var appearanceState: ActionCardState? = null
        var slotColor = Color.Unspecified
        val size = ActionCardSize(
            minHeight = { current ->
                sizeState = current
                if (current.selected) 112.dp else 96.dp
            },
        )
        val appearance = ActionCardAppearance(
            leadingColor = { current ->
                appearanceState = current
                if (current.enabled) Color.Green else Color.Gray
            },
        )
        composeRule.setContent {
            ActionCard(
                title = "Action",
                state = state.value,
                onClick = { clicks++ },
                modifier = Modifier.width(280.dp).testTag("card"),
                size = size,
                appearance = appearance,
                leading = { color -> SideEffect { slotColor = color } },
            )
        }

        val card = composeRule.onNodeWithTag("card")
        card.assertIsEnabled().assertIsNotSelected().assertHeightIsEqualTo(96.dp).performClick()
        composeRule.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(state.value, sizeState)
            assertEquals(state.value, appearanceState)
            assertEquals(Color.Green, slotColor)
            state.value = ActionCardState(enabled = false, selected = true)
        }

        card.assertIsNotEnabled().assertIsSelected().assertHeightIsEqualTo(112.dp).performClick()
        composeRule.runOnIdle {
            assertEquals(1, clicks)
            assertEquals(state.value, sizeState)
            assertEquals(state.value, appearanceState)
            assertEquals(Color.Gray, slotColor)
            state.value = ActionCardState(selected = true)
        }

        card.assertIsEnabled().assertIsSelected().performClick()
        composeRule.runOnIdle { assertEquals(2, clicks) }
    }
}
