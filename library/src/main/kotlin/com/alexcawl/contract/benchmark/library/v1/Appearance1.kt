package com.alexcawl.contract.benchmark.library.v1

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
interface Appearance1 {
    @Composable
    fun shape(): Shape

    @Composable
    fun containerColor(enabled: Boolean): Color

    @Composable
    fun contentColor(enabled: Boolean): Color

    @Composable
    fun shadowElevation(enabled: Boolean, interactionSource: InteractionSource): Dp

    @Composable
    fun border(): BorderStroke? = null
}

inline fun Appearance1(
    crossinline shape: @Composable Appearance1.() -> Shape,
    crossinline containerColor: @Composable Appearance1.(enabled: Boolean) -> Color,
    crossinline contentColor: @Composable Appearance1.(enabled: Boolean) -> Color,
    crossinline shadowElevation: @Composable Appearance1.(enabled: Boolean, interactionSource: InteractionSource) -> Dp,
    noinline border: (@Composable Appearance1.() -> BorderStroke?)? = null,
): Appearance1 = object : ReflectiveEqualsHashCode(), Appearance1 {
    @Composable
    override fun shape(): Shape = shape.invoke(this)

    @Composable
    override fun containerColor(enabled: Boolean): Color = containerColor.invoke(this, enabled)

    @Composable
    override fun contentColor(enabled: Boolean): Color = contentColor.invoke(this, enabled)

    @Composable
    override fun shadowElevation(enabled: Boolean, interactionSource: InteractionSource): Dp = shadowElevation.invoke(this, enabled, interactionSource)

    @Composable
    override fun border(): BorderStroke? = if (border != null) border.invoke(this) else super.border()
}

interface Appearance1Wrapper : Appearance1 {
    val wrapped: Appearance1
}

@RememberInComposition
inline fun Appearance1.copy(
    crossinline shape: @Composable Appearance1Wrapper.() -> Shape = { wrapped.shape() },
    crossinline containerColor: @Composable Appearance1Wrapper.(enabled: Boolean) -> Color = { wrapped.containerColor(it) },
    crossinline contentColor: @Composable Appearance1Wrapper.(enabled: Boolean) -> Color = { wrapped.contentColor(it) },
    crossinline shadowElevation: @Composable Appearance1Wrapper.(enabled: Boolean, interactionSource: InteractionSource) -> Dp = { enabled, interactionSource -> wrapped.shadowElevation(enabled, interactionSource) },
    crossinline border: @Composable Appearance1Wrapper.() -> BorderStroke? = { wrapped.border() },
): Appearance1 = object : ReflectiveEqualsHashCode(), Appearance1Wrapper {
    override val wrapped: Appearance1
        get() = this@copy

    @Composable
    override fun shape(): Shape = shape.invoke(this)

    @Composable
    override fun containerColor(enabled: Boolean): Color = containerColor.invoke(this, enabled)

    @Composable
    override fun contentColor(enabled: Boolean): Color = contentColor.invoke(this, enabled)

    @Composable
    override fun shadowElevation(enabled: Boolean, interactionSource: InteractionSource): Dp = shadowElevation.invoke(this, enabled, interactionSource)

    @Composable
    override fun border(): BorderStroke? = border.invoke(this)
}

@Composable
inline fun Appearance1.rememberCopy(
    crossinline shape: @Composable Appearance1Wrapper.() -> Shape = { wrapped.shape() },
    crossinline containerColor: @Composable Appearance1Wrapper.(enabled: Boolean) -> Color = { wrapped.containerColor(it) },
    crossinline contentColor: @Composable Appearance1Wrapper.(enabled: Boolean) -> Color = { wrapped.contentColor(it) },
    crossinline shadowElevation: @Composable Appearance1Wrapper.(enabled: Boolean, interactionSource: InteractionSource) -> Dp = { enabled, interactionSource -> wrapped.shadowElevation(enabled, interactionSource) },
    crossinline border: @Composable Appearance1Wrapper.() -> BorderStroke? = { wrapped.border() },
): Appearance1 = remember {
    copy(
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        shadowElevation = shadowElevation,
        border = border,
    )
}
