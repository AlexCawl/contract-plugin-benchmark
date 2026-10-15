package com.alexcawl.contract.benchmark.library.v2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
interface Appearance2 {
    fun shape(): Shape

    fun containerColor(enabled: Boolean): Color

    fun contentColor(enabled: Boolean): Color

    fun shadowElevation(enabled: Boolean, interaction: Interaction?): Dp {
        return if (!enabled) {
            0.dp
        } else {
            when (interaction) {
                is HoverInteraction.Enter -> 1.dp
                is DragInteraction.Start -> 6.dp
                else -> 0.dp
            }
        }
    }

    fun border(): BorderStroke? = null
}

object Appearance2Presets {
    val default: Appearance2
        @Composable
        get(): Appearance2 {
            val shape = MaterialTheme.shapes.medium
            val containerColor =  MaterialTheme.colorScheme.surfaceContainerHighest
            val contentColor =  MaterialTheme.colorScheme.contentColorFor(containerColor)
            val disabledContainerColor =  MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f).compositeOver(containerColor)
            val disabledContentColor = contentColor.copy(alpha = 0.38f)
            return remember(shape, containerColor, contentColor, disabledContainerColor, disabledContentColor) {
                Appearance2(
                    shape = { shape },
                    containerColor = { enabled ->
                        if (enabled) containerColor else disabledContainerColor
                    },
                    contentColor = { enabled ->
                        if (enabled) contentColor else disabledContentColor
                    },
                )
            }
        }
}

private class Appearance2Impl(
    private val shape: () -> Shape,
    private val containerColor: (enabled: Boolean) -> Color,
    private val contentColor: (enabled: Boolean) -> Color,
    private val shadowElevation: ((enabled: Boolean, interaction: Interaction?) -> Dp)?,
    private val border: (() -> BorderStroke?)?,
) : Appearance2 {
    override fun shape(): Shape = shape.invoke()

    override fun containerColor(enabled: Boolean): Color = containerColor.invoke(enabled)

    override fun contentColor(enabled: Boolean): Color = contentColor.invoke(enabled)

    override fun shadowElevation(enabled: Boolean, interaction: Interaction?): Dp = if (shadowElevation != null) shadowElevation.invoke(enabled, interaction) else super.shadowElevation(enabled, interaction)

    override fun border(): BorderStroke? = if (border != null) border.invoke() else super.border()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Appearance2Impl

        if (shape != other.shape) return false
        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (shadowElevation != other.shadowElevation) return false
        if (border != other.border) return false

        return true
    }

    override fun hashCode(): Int {
        var result = shape.hashCode()
        result = 31 * result + containerColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + (shadowElevation?.hashCode() ?: 0)
        result = 31 * result + (border?.hashCode() ?: 0)
        return result
    }
}

@JvmOverloads
fun Appearance2(
    shape: () -> Shape,
    containerColor: (enabled: Boolean) -> Color,
    contentColor: (enabled: Boolean) -> Color,
    shadowElevation: ((enabled: Boolean, interaction: Interaction?) -> Dp)? = null,
    border: (() -> BorderStroke?)? = null,
): Appearance2 = Appearance2Impl(shape, containerColor, contentColor, shadowElevation, border)

@JvmOverloads
fun Appearance2.copy(
    shape: () -> Shape = this::shape,
    containerColor: (enabled: Boolean) -> Color = this::containerColor,
    contentColor: (enabled: Boolean) -> Color = this::contentColor,
    shadowElevation: (enabled: Boolean, interaction: Interaction?) -> Dp = this::shadowElevation,
    border: () -> BorderStroke? = this::border,
): Appearance2 = Appearance2Impl(shape, containerColor, contentColor, shadowElevation, border)
