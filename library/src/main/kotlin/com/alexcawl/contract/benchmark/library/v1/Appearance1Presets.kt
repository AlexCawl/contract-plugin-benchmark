package com.alexcawl.contract.benchmark.library.v1

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
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object Appearance1Presets {

    val default: Appearance1
        @Composable
        get() = remember {
            Appearance1(
                shape = { CardDefaults.shape },
                containerColor = { enabled ->
                    val colors = CardDefaults.cardColors()
                    if (enabled) colors.containerColor else colors.disabledContainerColor
                },
                contentColor = { enabled ->
                    val colors = CardDefaults.cardColors()
                    if (enabled) colors.contentColor else colors.disabledContentColor
                },
                shadowElevation = { enabled, interactionSource ->
                    defaultCardShadowElevation(enabled, interactionSource)
                },
            )
        }
}

@Composable
private fun defaultCardShadowElevation(enabled: Boolean, interactionSource: InteractionSource): Dp {
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
    val target = if (!enabled) {
        0.dp
    } else {
        when (interaction) {
            is HoverInteraction.Enter -> 1.dp
            is DragInteraction.Start -> 6.dp
            else -> 0.dp
        }
    }
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
