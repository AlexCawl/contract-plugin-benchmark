package com.alexcawl.contract.benchmark.library.v3

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
interface Appearance3 {
    fun shape(): Shape

    fun containerColor(enabled: Boolean): Color

    fun contentColor(enabled: Boolean): Color

    fun shadowElevation(enabled: Boolean, interaction: Interaction?): Dp = if (!enabled) {
        0.dp
    } else {
        when (interaction) {
            is HoverInteraction.Enter -> 1.dp
            is DragInteraction.Start -> 6.dp
            else -> 0.dp
        }
    }

    fun border(): BorderStroke? = null

    fun contract(): Contract

    class Contract(
        val shape: Appearance3.() -> Shape,
        val containerColor: Appearance3.(enabled: Boolean) -> Color,
        val contentColor: Appearance3.(enabled: Boolean) -> Color,
        val shadowElevation: (Appearance3.(enabled: Boolean, interaction: Interaction?) -> Dp)? = null,
        val border: (Appearance3.() -> BorderStroke?)? = null,
    )
}

object Appearance3Presets {
    val default: Appearance3
        @Composable
        get(): Appearance3 {
            val shape = MaterialTheme.shapes.medium
            val containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val contentColor = MaterialTheme.colorScheme.contentColorFor(containerColor)
            val disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f).compositeOver(containerColor)
            val disabledContentColor = contentColor.copy(alpha = 0.38f)
            return remember(shape, containerColor, contentColor, disabledContainerColor, disabledContentColor) {
                Appearance3(
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

private class Appearance3Impl(
    private val shape: Appearance3.() -> Shape,
    private val containerColor: Appearance3.(enabled: Boolean) -> Color,
    private val contentColor: Appearance3.(enabled: Boolean) -> Color,
    private val shadowElevation: (Appearance3.(enabled: Boolean, interaction: Interaction?) -> Dp)?,
    private val border: (Appearance3.() -> BorderStroke?)?,
) : Appearance3 {
    override fun shape(): Shape = shape.invoke(this)

    override fun containerColor(enabled: Boolean): Color = containerColor.invoke(this, enabled)

    override fun contentColor(enabled: Boolean): Color = contentColor.invoke(this, enabled)

    override fun shadowElevation(enabled: Boolean, interaction: Interaction?): Dp {
        val callback = shadowElevation
        return if (callback == null) {
            super<Appearance3>.shadowElevation(enabled, interaction)
        } else {
            callback.invoke(this, enabled, interaction)
        }
    }

    override fun border(): BorderStroke? {
        val callback = border
        return if (callback == null) super<Appearance3>.border() else callback.invoke(this)
    }

    override fun contract(): Appearance3.Contract =
        Appearance3.Contract(shape, containerColor, contentColor, shadowElevation, border)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Appearance3Impl) return false
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

fun Appearance3(
    shape: Appearance3.() -> Shape,
    containerColor: Appearance3.(enabled: Boolean) -> Color,
    contentColor: Appearance3.(enabled: Boolean) -> Color,
    shadowElevation: (Appearance3.(enabled: Boolean, interaction: Interaction?) -> Dp)? = null,
    border: (Appearance3.() -> BorderStroke?)? = null,
): Appearance3 = Appearance3Impl(shape, containerColor, contentColor, shadowElevation, border)

fun Appearance3.copy(
    shape: (Appearance3.() -> Shape)? = null,
    containerColor: (Appearance3.(enabled: Boolean) -> Color)? = null,
    contentColor: (Appearance3.(enabled: Boolean) -> Color)? = null,
    shadowElevation: (Appearance3.(enabled: Boolean, interaction: Interaction?) -> Dp)? = null,
    border: (Appearance3.() -> BorderStroke?)? = null,
): Appearance3 {
    val original = contract()
    return Appearance3(
        shape = shape ?: original.shape,
        containerColor = containerColor ?: original.containerColor,
        contentColor = contentColor ?: original.contentColor,
        shadowElevation = shadowElevation ?: original.shadowElevation,
        border = border ?: original.border,
    )
}
