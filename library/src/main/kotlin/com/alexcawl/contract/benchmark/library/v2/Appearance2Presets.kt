package com.alexcawl.contract.benchmark.library.v2

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.compositeOver

object Appearance2Presets {
    val default: Appearance2
        @Composable
        get(): Appearance2 {
            val shape = MaterialTheme.shapes.medium
            val containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val contentColor = MaterialTheme.colorScheme.contentColorFor(containerColor)
            val disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f).compositeOver(containerColor)
            val disabledContentColor = contentColor.copy(alpha = 0.38f)
            return remember(
                shape,
                containerColor,
                contentColor,
                disabledContainerColor,
                disabledContentColor
            ) {
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