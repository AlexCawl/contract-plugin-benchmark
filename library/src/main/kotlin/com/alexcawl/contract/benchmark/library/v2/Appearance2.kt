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

    fun contract(): Contract

    class Contract(
        val shape: Appearance2.() -> Shape,
        val containerColor: Appearance2.(enabled: Boolean) -> Color,
        val contentColor: Appearance2.(enabled: Boolean) -> Color,
        val shadowElevation: (Appearance2.(enabled: Boolean, interaction: Interaction?) -> Dp)? = null,
        val border: (Appearance2.() -> BorderStroke?)? = null,
    )
}

object Appearance2Presets {
    val default: Appearance2
        @Composable
        get(): Appearance2 {
            val shape = MaterialTheme.shapes.medium
            val containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val contentColor = MaterialTheme.colorScheme.contentColorFor(containerColor)
            val disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f).compositeOver(containerColor)
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
    private val shape: Appearance2.() -> Shape,
    private val containerColor: Appearance2.(enabled: Boolean) -> Color,
    private val contentColor: Appearance2.(enabled: Boolean) -> Color,
    private val shadowElevation: (Appearance2.(enabled: Boolean, interaction: Interaction?) -> Dp)?,
    private val border: (Appearance2.() -> BorderStroke?)?,
) : Appearance2 {
    override fun shape(): Shape = shape.invoke(this)

    override fun containerColor(enabled: Boolean): Color = containerColor.invoke(this, enabled)

    override fun contentColor(enabled: Boolean): Color = contentColor.invoke(this, enabled)

    override fun shadowElevation(enabled: Boolean, interaction: Interaction?): Dp {
        val callback = shadowElevation
        return if (callback == null) {
            super<Appearance2>.shadowElevation(enabled, interaction)
        } else {
            callback.invoke(this, enabled, interaction)
        }
    }

    override fun border(): BorderStroke? {
        val callback = border
        return if (callback == null) super<Appearance2>.border() else callback.invoke(this)
    }

    override fun contract(): Appearance2.Contract =
        Appearance2.Contract(shape, containerColor, contentColor, shadowElevation, border)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Appearance2Impl) return false
        return shape == other.shape &&
            containerColor == other.containerColor &&
            contentColor == other.contentColor &&
            shadowElevation == other.shadowElevation &&
            border == other.border
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

fun Appearance2(
    shape: Appearance2.() -> Shape,
    containerColor: Appearance2.(enabled: Boolean) -> Color,
    contentColor: Appearance2.(enabled: Boolean) -> Color,
    shadowElevation: (Appearance2.(enabled: Boolean, interaction: Interaction?) -> Dp)? = null,
    border: (Appearance2.() -> BorderStroke?)? = null,
): Appearance2 = Appearance2Impl(shape, containerColor, contentColor, shadowElevation, border)

fun Appearance2.copy(
    shape: (Appearance2.() -> Shape)? = null,
    containerColor: (Appearance2.(enabled: Boolean) -> Color)? = null,
    contentColor: (Appearance2.(enabled: Boolean) -> Color)? = null,
    shadowElevation: (Appearance2.(enabled: Boolean, interaction: Interaction?) -> Dp)? = null,
    border: (Appearance2.() -> BorderStroke?)? = null,
): Appearance2 {
    val original = contract()
    return Appearance2(
        shape = shape ?: original.shape,
        containerColor = containerColor ?: original.containerColor,
        contentColor = contentColor ?: original.contentColor,
        shadowElevation = shadowElevation ?: original.shadowElevation,
        border = border ?: original.border,
    )
}
