package com.alexcawl.contract.benchmark.app.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.alexcawl.contract.benchmark.library.common.ActionCardState

/** Shared renderer; every visual value is supplied by the caller. */
@Composable
fun ActionCard(
    title: String,
    description: String?,
    badge: String?,
    state: ActionCardState,
    onClick: () -> Unit,
    modifier: Modifier,
    minHeight: Dp,
    contentPadding: PaddingValues,
    horizontalSpacing: Dp,
    verticalSpacing: Dp,
    leadingSize: Dp,
    trailingSize: Dp,
    badgePadding: PaddingValues,
    shape: Shape,
    backgroundBrush: Brush?,
    borderStroke: BorderStroke?,
    titleTextStyle: TextStyle,
    descriptionTextStyle: TextStyle,
    badgeTextStyle: TextStyle,
    badgeShape: Shape,
    badgeBackgroundBrush: Brush?,
    leadingColor: Color,
    trailingColor: Color,
    leading: (@Composable (Color) -> Unit)?,
    trailing: (@Composable (Color) -> Unit)?,
) {
    Row(
        modifier = modifier
            .heightIn(min = minHeight)
            .clip(shape)
            .then(if (backgroundBrush != null) Modifier.background(backgroundBrush) else Modifier)
            .then(if (borderStroke != null) Modifier.border(borderStroke, shape) else Modifier)
            .selectable(
                selected = state.selected,
                enabled = state.enabled,
                role = Role.Button,
                onClick = onClick,
            )
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leading != null) {
            Box(Modifier.size(leadingSize), contentAlignment = Alignment.Center) {
                leading(leadingColor)
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(verticalSpacing),
        ) {
            BasicText(
                text = title,
                style = titleTextStyle,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (description != null) {
                BasicText(
                    text = description,
                    style = descriptionTextStyle,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (badge != null) {
                BasicText(
                    text = badge,
                    modifier = Modifier
                        .clip(badgeShape)
                        .then(
                            if (badgeBackgroundBrush != null) {
                                Modifier.background(badgeBackgroundBrush)
                            } else {
                                Modifier
                            }
                        )
                        .padding(badgePadding),
                    style = badgeTextStyle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        if (trailing != null) {
            Box(Modifier.size(trailingSize), contentAlignment = Alignment.Center) {
                trailing(trailingColor)
            }
        }
    }
}
