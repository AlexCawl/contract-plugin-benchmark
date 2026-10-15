package com.alexcawl.contract.benchmark.app.v3

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun Card3(
    onClick: () -> Unit,
    size: Size3,
    appearance: Appearance3,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = appearance.shape(),
        color = appearance.containerColor(enabled),
        contentColor = appearance.contentColor(enabled),
        shadowElevation = appearance.animatedShadowElevation(enabled, interactionSource),
        border = appearance.border(),
        interactionSource = interactionSource,
    ) {
        Column(
            verticalArrangement = size.contentArrangement(),
            horizontalAlignment = size.contentAlignment(),
            content = content,
        )
    }
}

/** Filled Card elevation transitions from Material3 1.4.0, matching V1. */
@Composable
private fun Appearance3.animatedShadowElevation(
    enabled: Boolean,
    interactionSource: InteractionSource,
): Dp {
    val interactions = remember(interactionSource) { mutableStateListOf<Interaction>() }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press,
                is HoverInteraction.Enter,
                is FocusInteraction.Focus,
                is DragInteraction.Start -> interactions.add(interaction)
                is PressInteraction.Release -> interactions.remove(interaction.press)
                is PressInteraction.Cancel -> interactions.remove(interaction.press)
                is HoverInteraction.Exit -> interactions.remove(interaction.enter)
                is FocusInteraction.Unfocus -> interactions.remove(interaction.focus)
                is DragInteraction.Stop -> interactions.remove(interaction.start)
                is DragInteraction.Cancel -> interactions.remove(interaction.start)
            }
        }
    }
    val interaction = interactions.lastOrNull()
    val target = shadowElevation(enabled, interaction)
    val elevation = remember { Animatable(target, Dp.VectorConverter) }
    LaunchedEffect(target, enabled) {
        if (!enabled) {
            elevation.snapTo(target)
        } else if (elevation.targetValue != target) {
            val spec = if (interaction != null) {
                tween<Dp>(durationMillis = 120, easing = FastOutSlowInEasing)
            } else {
                tween(
                    durationMillis = if (elevation.targetValue == 1.dp) 120 else 150,
                    easing = CubicBezierEasing(0.40f, 0.00f, 0.60f, 1.00f),
                )
            }
            elevation.animateTo(target, spec)
        }
    }
    return elevation.value
}
