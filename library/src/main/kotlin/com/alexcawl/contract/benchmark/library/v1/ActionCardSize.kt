package com.alexcawl.contract.benchmark.library.v1

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.benchmark.library.common.ActionCardState

/** Geometry of ActionCard. Typography belongs to ActionCardAppearance. */
@Stable
interface ActionCardSize {
    @Composable
    fun minHeight(state: ActionCardState): Dp = 88.dp

    @Composable
    fun contentPadding(state: ActionCardState): PaddingValues = PaddingValues(16.dp)

    @Composable
    fun horizontalSpacing(state: ActionCardState): Dp = 12.dp

    @Composable
    fun verticalSpacing(state: ActionCardState): Dp = 4.dp

    @Composable
    fun leadingSize(state: ActionCardState): Dp = 40.dp

    @Composable
    fun trailingSize(state: ActionCardState): Dp = 24.dp

    @Composable
    fun badgePadding(state: ActionCardState): PaddingValues = PaddingValues(horizontal = 8.dp, vertical = 4.dp)

    companion object Default : ActionCardSize
}

/** V1 factory: inline receiver lambdas captured by an anonymous reflective implementation. */
inline fun ActionCardSize(
    crossinline minHeight: @Composable ActionCardSize.(state: ActionCardState) -> Dp = { _ ->
        88.dp
    },
    crossinline contentPadding: @Composable ActionCardSize.(state: ActionCardState) -> PaddingValues = { _ ->
        PaddingValues(16.dp)
    },
    crossinline horizontalSpacing: @Composable ActionCardSize.(state: ActionCardState) -> Dp = { _ ->
        12.dp
    },
    crossinline verticalSpacing: @Composable ActionCardSize.(state: ActionCardState) -> Dp = { _ ->
        4.dp
    },
    crossinline leadingSize: @Composable ActionCardSize.(state: ActionCardState) -> Dp = { _ ->
        40.dp
    },
    crossinline trailingSize: @Composable ActionCardSize.(state: ActionCardState) -> Dp = { _ ->
        24.dp
    },
    crossinline badgePadding: @Composable ActionCardSize.(state: ActionCardState) -> PaddingValues = { _ ->
        PaddingValues(horizontal = 8.dp, vertical = 4.dp)
    },
): ActionCardSize = object : ReflectiveEqualsHashCode(), ActionCardSize {
    @Composable
    override fun minHeight(state: ActionCardState): Dp = minHeight.invoke(this, state)

    @Composable
    override fun contentPadding(state: ActionCardState): PaddingValues = contentPadding.invoke(this, state)

    @Composable
    override fun horizontalSpacing(state: ActionCardState): Dp = horizontalSpacing.invoke(this, state)

    @Composable
    override fun verticalSpacing(state: ActionCardState): Dp = verticalSpacing.invoke(this, state)

    @Composable
    override fun leadingSize(state: ActionCardState): Dp = leadingSize.invoke(this, state)

    @Composable
    override fun trailingSize(state: ActionCardState): Dp = trailingSize.invoke(this, state)

    @Composable
    override fun badgePadding(state: ActionCardState): PaddingValues = badgePadding.invoke(this, state)
}

/** Receiver for copy overrides. Use [wrapped] to call the original without recursion. */
interface ActionCardSizeWrapper : ActionCardSize {
    val wrapped: ActionCardSize
}

/**
 * Overrides selected methods, delegating all others to [ActionCardSizeWrapper.wrapped].
 * Calling this.method() inside its own override recurses; use wrapped.method() instead.
 * In composition, prefer [rememberCopy].
 */
@RememberInComposition
inline fun ActionCardSize.copy(
    crossinline minHeight: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.minHeight(state) },
    crossinline contentPadding: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> PaddingValues = { state -> wrapped.contentPadding(state) },
    crossinline horizontalSpacing: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.horizontalSpacing(state) },
    crossinline verticalSpacing: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.verticalSpacing(state) },
    crossinline leadingSize: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.leadingSize(state) },
    crossinline trailingSize: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.trailingSize(state) },
    crossinline badgePadding: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> PaddingValues = { state -> wrapped.badgePadding(state) },
): ActionCardSize = object : ReflectiveEqualsHashCode(), ActionCardSizeWrapper {
    override val wrapped: ActionCardSize
        get() = this@copy

    @Composable
    override fun minHeight(state: ActionCardState): Dp = minHeight.invoke(this, state)

    @Composable
    override fun contentPadding(state: ActionCardState): PaddingValues = contentPadding.invoke(this, state)

    @Composable
    override fun horizontalSpacing(state: ActionCardState): Dp = horizontalSpacing.invoke(this, state)

    @Composable
    override fun verticalSpacing(state: ActionCardState): Dp = verticalSpacing.invoke(this, state)

    @Composable
    override fun leadingSize(state: ActionCardState): Dp = leadingSize.invoke(this, state)

    @Composable
    override fun trailingSize(state: ActionCardState): Dp = trailingSize.invoke(this, state)

    @Composable
    override fun badgePadding(state: ActionCardState): PaddingValues = badgePadding.invoke(this, state)
}

/**
 * Remembers the copy without keys, matching V1: the original receiver and override lambdas
 * are captured on the first composition. Read State or rememberUpdatedState in overrides
 * when captured values must change.
 */
@Composable
inline fun ActionCardSize.rememberCopy(
    crossinline minHeight: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.minHeight(state) },
    crossinline contentPadding: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> PaddingValues = { state -> wrapped.contentPadding(state) },
    crossinline horizontalSpacing: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.horizontalSpacing(state) },
    crossinline verticalSpacing: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.verticalSpacing(state) },
    crossinline leadingSize: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.leadingSize(state) },
    crossinline trailingSize: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> Dp = { state -> wrapped.trailingSize(state) },
    crossinline badgePadding: @Composable ActionCardSizeWrapper.(state: ActionCardState) -> PaddingValues = { state -> wrapped.badgePadding(state) },
): ActionCardSize = remember {
    copy(
        minHeight = minHeight,
        contentPadding = contentPadding,
        horizontalSpacing = horizontalSpacing,
        verticalSpacing = verticalSpacing,
        leadingSize = leadingSize,
        trailingSize = trailingSize,
        badgePadding = badgePadding,
    )
}
