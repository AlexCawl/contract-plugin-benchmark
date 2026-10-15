package com.alexcawl.contract.benchmark.library.v2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

object Size2Presets {
    val default: Size2
        @Composable
        get() = remember {
            Size2(
                contentArrangement = { Arrangement.Top },
                contentAlignment = { Alignment.Start },
            )
        }
}