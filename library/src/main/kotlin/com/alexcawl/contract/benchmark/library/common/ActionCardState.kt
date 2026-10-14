package com.alexcawl.contract.benchmark.library.common

import androidx.compose.runtime.Immutable

@Immutable
data class ActionCardState(
    val enabled: Boolean = true,
    val selected: Boolean = false,
)
