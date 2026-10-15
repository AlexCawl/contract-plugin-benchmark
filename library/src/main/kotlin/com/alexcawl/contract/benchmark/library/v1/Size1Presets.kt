package com.alexcawl.contract.benchmark.library.v1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

object Size1Presets {

    val default: Size1
        @Composable
        get() = remember {
            Size1(
                contentArrangement = { Arrangement.Top },
                contentAlignment = { Alignment.Start },
            )
        }
}