package com.alexcawl.contract.benchmark.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alexcawl.contract.benchmark.app.v1.ActionCard
import com.alexcawl.contract.benchmark.library.common.ActionCardState
import com.alexcawl.contract.benchmark.library.v1.ActionCardAppearance
import com.alexcawl.contract.benchmark.library.v1.ActionCardSize
import com.alexcawl.contract.benchmark.library.v1.rememberCopy

@Composable
fun BenchmarkScene() {
    var selected by remember { mutableStateOf(false) }
    val compactSize = remember {
        ActionCardSize(
            minHeight = { 72.dp },
            contentPadding = { PaddingValues(12.dp) },
            leadingSize = { 32.dp },
        )
    }
    val emphasizedAppearance = ActionCardAppearance.Default.rememberCopy(
        titleTextStyle = { state ->
            wrapped.titleTextStyle(state).copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        },
    )

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ActionCard(
            title = "Standard action",
            description = "Tap to change the selected state",
            badge = "V1",
            state = ActionCardState(selected = selected),
            onClick = { selected = !selected },
            modifier = Modifier.fillMaxWidth(),
            leading = { color -> Box(Modifier.size(24.dp).background(color, CircleShape)) },
            trailing = { color -> BasicText("›", style = TextStyle(color = color, fontSize = 24.sp)) },
        )
        ActionCard(
            title = "Selected action",
            description = "Compact geometry and custom typography",
            state = ActionCardState(selected = true),
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
            size = compactSize,
            appearance = emphasizedAppearance,
        )
        ActionCard(
            title = "Unavailable action",
            description = "Disabled cards ignore clicks",
            badge = "Unavailable",
            state = ActionCardState(enabled = false),
            onClick = {},
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
