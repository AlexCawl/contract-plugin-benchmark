package com.alexcawl.contract.benchmark.app.v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.ReportDrawn
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.benchmark.library.v2.Appearance2
import com.alexcawl.contract.benchmark.library.v2.Appearance2Presets
import com.alexcawl.contract.benchmark.library.v2.Card2
import com.alexcawl.contract.benchmark.library.v2.Size2
import com.alexcawl.contract.benchmark.library.v2.Size2Presets
import com.alexcawl.contract.benchmark.library.v2.copy

class Activity2 : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme()) {
                Scene2()
                ReportDrawn()
            }
        }
    }
}

@Composable
private fun Scene2() {
    var revision: Int by remember { mutableIntStateOf(0) }
    var enabled: Boolean by remember { mutableStateOf(true) }
    val alternate: Boolean by remember { derivedStateOf { revision % 2 != 0 } }

    val defaultSize = Size2Presets.default
    val defaultAppearance = Appearance2Presets.default

    val contentArrangement: Arrangement.Vertical = if (alternate) Arrangement.spacedBy(8.dp) else Arrangement.Top
    val contentAlignment = if (alternate) Alignment.CenterHorizontally else Alignment.Start
    val shape = RoundedCornerShape(if (alternate) 20.dp else 12.dp)
    val containerColor = if (alternate) Color(0xFFD0E8DC) else Color(0xFFE8DEF8)
    val contentColor = if (alternate) Color(0xFF102E22) else Color(0xFF1D192B)
    val disabledContainerColor = if (alternate) Color(0xFFD8DED9) else Color(0xFFE0E0E0)
    val disabledContentColor = if (alternate) Color(0xFF58605A) else Color(0xFF757575)
    val shadowElevation = 2.dp
    val border = if (alternate) BorderStroke(1.dp, Color(0xFF286B4C)) else null

    Column(
        Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .padding(12.dp)
            .semantics { testTagsAsResourceId = true },
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("V2 · 1000 cards")
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Button(
                onClick = { revision++ },
                modifier = Modifier
                    .testTag("change_appearance")
                    .semantics { stateDescription = "revision:$revision" },
            ) {
                Text(text = "Оформление: $revision")
            }
            Switch(
                checked = enabled,
                onCheckedChange = { enabled = it },
                modifier = Modifier.testTag("toggle_enabled"),
            )
            Text(text = "Enabled")
        }
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .testTag("contract_list"),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(count = 1000, key = { it }) { index: Int ->
                val size = defaultSize.rememberCopy(
                    contentArrangement = contentArrangement,
                    contentAlignment = contentAlignment,
                )
                val appearance = defaultAppearance.rememberCopy(
                    shape = shape,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = disabledContainerColor,
                    disabledContentColor = disabledContentColor,
                    shadowElevation = shadowElevation,
                    border = border,
                )
                val source = remember { MutableInteractionSource() }
                val pressed by source.collectIsPressedAsState()
                Card2(
                    onClick = {},
                    size = size,
                    appearance = appearance,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(112.dp)
                        .testTag("card_$index")
                        .semantics {
                            stateDescription = when {
                                !enabled -> "disabled"
                                pressed -> "pressed"
                                else -> "idle"
                            }
                        },
                    enabled = enabled,
                    interactionSource = source,
                ) {
                    Text(
                        text = "Карточка $index",
                        modifier = Modifier.padding(12.dp),
                    )
                    Text(
                        text = "Одинаковая нагрузка для двух контрактов",
                        modifier = Modifier.padding(horizontal = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun Size2.rememberCopy(
    contentArrangement: Arrangement.Vertical,
    contentAlignment: Alignment.Horizontal,
): Size2 = remember(contentArrangement, contentAlignment) {
    copy(
        contentArrangement = { contentArrangement },
        contentAlignment = { contentAlignment },
    )
}

@Composable
private fun Appearance2.rememberCopy(
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    disabledContainerColor: Color,
    disabledContentColor: Color,
    shadowElevation: Dp,
    border: BorderStroke?,
): Appearance2 = remember(shape, containerColor, contentColor, disabledContainerColor, disabledContentColor, shadowElevation, border) {
    copy(
        shape = { shape },
        containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
        contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
        shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
        border = { border },
    )
}
