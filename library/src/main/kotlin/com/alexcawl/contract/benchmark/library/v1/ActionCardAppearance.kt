package com.alexcawl.contract.benchmark.library.v1

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexcawl.contract.benchmark.library.common.ActionCardState

/** Visual styling and all typography of ActionCard. */
@Stable
interface ActionCardAppearance {
    @Composable
    fun shape(state: ActionCardState): Shape = remember { RoundedCornerShape(16.dp) }

    @Composable
    fun backgroundBrush(state: ActionCardState): Brush? = SolidColor(
        when {
            !state.enabled -> Color(0xFFF1F3F5)
            state.selected -> Color(0xFFE8F0FE)
            else -> Color(0xFFFFFFFF)
        },
    )

    @Composable
    fun borderStroke(state: ActionCardState): BorderStroke? = BorderStroke(
        1.dp,
        when {
            !state.enabled -> Color(0xFFE1E4E8)
            state.selected -> Color(0xFF2463EB)
            else -> Color(0xFFCCD2DA)
        },
    )

    @Composable
    fun titleTextStyle(state: ActionCardState): TextStyle = TextStyle(
        color = if (state.enabled) Color(0xFF172B4D) else Color(0xFF8993A4),
        fontSize = 16.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.SemiBold,
    )

    @Composable
    fun descriptionTextStyle(state: ActionCardState): TextStyle = TextStyle(
        color = if (state.enabled) Color(0xFF536176) else Color(0xFF8993A4),
        fontSize = 14.sp,
        lineHeight = 20.sp,
    )

    @Composable
    fun badgeTextStyle(state: ActionCardState): TextStyle = TextStyle(
        color = if (state.enabled) Color(0xFF174EA6) else Color(0xFF8993A4),
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    )

    @Composable
    fun badgeShape(state: ActionCardState): Shape = remember { RoundedCornerShape(6.dp) }

    @Composable
    fun badgeBackgroundBrush(state: ActionCardState): Brush? = SolidColor(
        if (state.enabled) Color(0xFFDDE9FF) else Color(0xFFE1E4E8),
    )

    @Composable
    fun leadingColor(state: ActionCardState): Color = if (state.enabled) Color(0xFF2463EB) else Color(0xFF8993A4)

    @Composable
    fun trailingColor(state: ActionCardState): Color = if (state.enabled) Color(0xFF536176) else Color(0xFF8993A4)

    companion object Default : ActionCardAppearance
}

/** V1 factory: inline receiver lambdas captured by an anonymous reflective implementation. */
inline fun ActionCardAppearance(
    crossinline shape: @Composable ActionCardAppearance.(state: ActionCardState) -> Shape = { _ ->
        remember { RoundedCornerShape(16.dp) }
    },
    crossinline backgroundBrush: @Composable ActionCardAppearance.(state: ActionCardState) -> Brush? = { state ->
        SolidColor(
            when {
                !state.enabled -> Color(0xFFF1F3F5)
                state.selected -> Color(0xFFE8F0FE)
                else -> Color(0xFFFFFFFF)
            },
        )
    },
    crossinline borderStroke: @Composable ActionCardAppearance.(state: ActionCardState) -> BorderStroke? = { state ->
        BorderStroke(
            1.dp,
            when {
                !state.enabled -> Color(0xFFE1E4E8)
                state.selected -> Color(0xFF2463EB)
                else -> Color(0xFFCCD2DA)
            },
        )
    },
    crossinline titleTextStyle: @Composable ActionCardAppearance.(state: ActionCardState) -> TextStyle = { state ->
        TextStyle(
            color = if (state.enabled) Color(0xFF172B4D) else Color(0xFF8993A4),
            fontSize = 16.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
        )
    },
    crossinline descriptionTextStyle: @Composable ActionCardAppearance.(state: ActionCardState) -> TextStyle = { state ->
        TextStyle(
            color = if (state.enabled) Color(0xFF536176) else Color(0xFF8993A4),
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
    },
    crossinline badgeTextStyle: @Composable ActionCardAppearance.(state: ActionCardState) -> TextStyle = { state ->
        TextStyle(
            color = if (state.enabled) Color(0xFF174EA6) else Color(0xFF8993A4),
            fontSize = 12.sp,
            lineHeight = 16.sp,
            fontWeight = FontWeight.Medium,
        )
    },
    crossinline badgeShape: @Composable ActionCardAppearance.(state: ActionCardState) -> Shape = { _ ->
        remember { RoundedCornerShape(6.dp) }
    },
    crossinline badgeBackgroundBrush: @Composable ActionCardAppearance.(state: ActionCardState) -> Brush? = { state ->
        SolidColor(
            if (state.enabled) Color(0xFFDDE9FF) else Color(0xFFE1E4E8),
        )
    },
    crossinline leadingColor: @Composable ActionCardAppearance.(state: ActionCardState) -> Color = { state ->
        if (state.enabled) Color(0xFF2463EB) else Color(0xFF8993A4)
    },
    crossinline trailingColor: @Composable ActionCardAppearance.(state: ActionCardState) -> Color = { state ->
        if (state.enabled) Color(0xFF536176) else Color(0xFF8993A4)
    },
): ActionCardAppearance = object : ReflectiveEqualsHashCode(), ActionCardAppearance {
    @Composable
    override fun shape(state: ActionCardState): Shape = shape.invoke(this, state)

    @Composable
    override fun backgroundBrush(state: ActionCardState): Brush? = backgroundBrush.invoke(this, state)

    @Composable
    override fun borderStroke(state: ActionCardState): BorderStroke? = borderStroke.invoke(this, state)

    @Composable
    override fun titleTextStyle(state: ActionCardState): TextStyle = titleTextStyle.invoke(this, state)

    @Composable
    override fun descriptionTextStyle(state: ActionCardState): TextStyle = descriptionTextStyle.invoke(this, state)

    @Composable
    override fun badgeTextStyle(state: ActionCardState): TextStyle = badgeTextStyle.invoke(this, state)

    @Composable
    override fun badgeShape(state: ActionCardState): Shape = badgeShape.invoke(this, state)

    @Composable
    override fun badgeBackgroundBrush(state: ActionCardState): Brush? = badgeBackgroundBrush.invoke(this, state)

    @Composable
    override fun leadingColor(state: ActionCardState): Color = leadingColor.invoke(this, state)

    @Composable
    override fun trailingColor(state: ActionCardState): Color = trailingColor.invoke(this, state)
}

/** Receiver for copy overrides. Use [wrapped] to call the original without recursion. */
interface ActionCardAppearanceWrapper : ActionCardAppearance {
    val wrapped: ActionCardAppearance
}

/**
 * Overrides selected methods, delegating all others to [ActionCardAppearanceWrapper.wrapped].
 * Calling this.method() inside its own override recurses; use wrapped.method() instead.
 * In composition, prefer [rememberCopy].
 */
@RememberInComposition
inline fun ActionCardAppearance.copy(
    crossinline shape: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Shape = { state -> wrapped.shape(state) },
    crossinline backgroundBrush: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Brush? = { state -> wrapped.backgroundBrush(state) },
    crossinline borderStroke: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> BorderStroke? = { state -> wrapped.borderStroke(state) },
    crossinline titleTextStyle: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> TextStyle = { state -> wrapped.titleTextStyle(state) },
    crossinline descriptionTextStyle: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> TextStyle = { state -> wrapped.descriptionTextStyle(state) },
    crossinline badgeTextStyle: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> TextStyle = { state -> wrapped.badgeTextStyle(state) },
    crossinline badgeShape: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Shape = { state -> wrapped.badgeShape(state) },
    crossinline badgeBackgroundBrush: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Brush? = { state -> wrapped.badgeBackgroundBrush(state) },
    crossinline leadingColor: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Color = { state -> wrapped.leadingColor(state) },
    crossinline trailingColor: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Color = { state -> wrapped.trailingColor(state) },
): ActionCardAppearance = object : ReflectiveEqualsHashCode(), ActionCardAppearanceWrapper {
    override val wrapped: ActionCardAppearance
        get() = this@copy

    @Composable
    override fun shape(state: ActionCardState): Shape = shape.invoke(this, state)

    @Composable
    override fun backgroundBrush(state: ActionCardState): Brush? = backgroundBrush.invoke(this, state)

    @Composable
    override fun borderStroke(state: ActionCardState): BorderStroke? = borderStroke.invoke(this, state)

    @Composable
    override fun titleTextStyle(state: ActionCardState): TextStyle = titleTextStyle.invoke(this, state)

    @Composable
    override fun descriptionTextStyle(state: ActionCardState): TextStyle = descriptionTextStyle.invoke(this, state)

    @Composable
    override fun badgeTextStyle(state: ActionCardState): TextStyle = badgeTextStyle.invoke(this, state)

    @Composable
    override fun badgeShape(state: ActionCardState): Shape = badgeShape.invoke(this, state)

    @Composable
    override fun badgeBackgroundBrush(state: ActionCardState): Brush? = badgeBackgroundBrush.invoke(this, state)

    @Composable
    override fun leadingColor(state: ActionCardState): Color = leadingColor.invoke(this, state)

    @Composable
    override fun trailingColor(state: ActionCardState): Color = trailingColor.invoke(this, state)
}

/**
 * Remembers the copy without keys, matching V1: the original receiver and override lambdas
 * are captured on the first composition. Read State or rememberUpdatedState in overrides
 * when captured values must change.
 */
@Composable
inline fun ActionCardAppearance.rememberCopy(
    crossinline shape: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Shape = { state -> wrapped.shape(state) },
    crossinline backgroundBrush: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Brush? = { state -> wrapped.backgroundBrush(state) },
    crossinline borderStroke: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> BorderStroke? = { state -> wrapped.borderStroke(state) },
    crossinline titleTextStyle: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> TextStyle = { state -> wrapped.titleTextStyle(state) },
    crossinline descriptionTextStyle: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> TextStyle = { state -> wrapped.descriptionTextStyle(state) },
    crossinline badgeTextStyle: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> TextStyle = { state -> wrapped.badgeTextStyle(state) },
    crossinline badgeShape: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Shape = { state -> wrapped.badgeShape(state) },
    crossinline badgeBackgroundBrush: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Brush? = { state -> wrapped.badgeBackgroundBrush(state) },
    crossinline leadingColor: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Color = { state -> wrapped.leadingColor(state) },
    crossinline trailingColor: @Composable ActionCardAppearanceWrapper.(state: ActionCardState) -> Color = { state -> wrapped.trailingColor(state) },
): ActionCardAppearance = remember {
    copy(
        shape = shape,
        backgroundBrush = backgroundBrush,
        borderStroke = borderStroke,
        titleTextStyle = titleTextStyle,
        descriptionTextStyle = descriptionTextStyle,
        badgeTextStyle = badgeTextStyle,
        badgeShape = badgeShape,
        badgeBackgroundBrush = badgeBackgroundBrush,
        leadingColor = leadingColor,
        trailingColor = trailingColor,
    )
}
