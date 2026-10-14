package com.alexcawl.contract.benchmark.app.v1

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.alexcawl.contract.benchmark.app.common.ActionCard as CommonActionCard
import com.alexcawl.contract.benchmark.library.common.ActionCardState
import com.alexcawl.contract.benchmark.library.v1.ActionCardAppearance
import com.alexcawl.contract.benchmark.library.v1.ActionCardSize

@Composable
fun ActionCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    description: String? = null,
    badge: String? = null,
    state: ActionCardState = ActionCardState(),
    size: ActionCardSize = ActionCardSize.Default,
    appearance: ActionCardAppearance = ActionCardAppearance.Default,
    leading: (@Composable (Color) -> Unit)? = null,
    trailing: (@Composable (Color) -> Unit)? = null,
) {
    CommonActionCard(
        title = title,
        description = description,
        badge = badge,
        state = state,
        onClick = onClick,
        modifier = modifier,
        minHeight = size.minHeight(state),
        contentPadding = size.contentPadding(state),
        horizontalSpacing = size.horizontalSpacing(state),
        verticalSpacing = size.verticalSpacing(state),
        leadingSize = size.leadingSize(state),
        trailingSize = size.trailingSize(state),
        badgePadding = size.badgePadding(state),
        shape = appearance.shape(state),
        backgroundBrush = appearance.backgroundBrush(state),
        borderStroke = appearance.borderStroke(state),
        titleTextStyle = appearance.titleTextStyle(state),
        descriptionTextStyle = appearance.descriptionTextStyle(state),
        badgeTextStyle = appearance.badgeTextStyle(state),
        badgeShape = appearance.badgeShape(state),
        badgeBackgroundBrush = appearance.badgeBackgroundBrush(state),
        leadingColor = appearance.leadingColor(state),
        trailingColor = appearance.trailingColor(state),
        leading = leading,
        trailing = trailing,
    )
}
