package com.alexcawl.contract.benchmark.library.v1

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun Card1(
    onClick: () -> Unit,
    size: Size1,
    appearance: Appearance1,
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
        shadowElevation = appearance.shadowElevation(enabled, interactionSource),
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
