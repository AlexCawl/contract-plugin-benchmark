package com.alexcawl.contract.benchmark.test.v2

import androidx.benchmark.BlackHole
import androidx.benchmark.ExperimentalBenchmarkConfigApi
import androidx.benchmark.MicrobenchmarkConfig
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.alexcawl.contract.benchmark.library.v2.Appearance2
import com.alexcawl.contract.benchmark.library.v2.copy
import com.alexcawl.contract.benchmark.test.AllocationBytesCapture
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalBenchmarkConfigApi::class)
class Appearance2Benchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule(
        if (InstrumentationRegistry.getArguments().getString("allocationBytes") == "true") {
            MicrobenchmarkConfig(metrics = listOf(AllocationBytesCapture()))
        } else {
            MicrobenchmarkConfig()
        },
    )

    @Suppress("NOTHING_TO_INLINE")
    private inline fun appearance(
        shape: Shape = RoundedCornerShape(12.dp),
        containerColor: Color = Color(0xFFE8DEF8),
        contentColor: Color = Color(0xFF1D192B),
        disabledContainerColor: Color = Color(0xFFE0E0E0),
        disabledContentColor: Color = Color(0xFF757575),
        shadowElevation: Dp = 0.dp,
        border: BorderStroke? = null,
    ): Appearance2 = Appearance2(
        shape = { shape },
        containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
        contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
        shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
        border = { border },
    )

    @Test
    fun create() {
        val shape = RoundedCornerShape(12.dp)
        val containerColor = Color(0xFFE8DEF8)
        val contentColor = Color(0xFF1D192B)
        val disabledContainerColor = Color(0xFFE0E0E0)
        val disabledContentColor = Color(0xFF757575)
        val shadowElevation = 0.dp
        val border: BorderStroke? = null
        benchmarkRule.measureRepeated {
            BlackHole.consume(
                appearance(
                    shape = shape,
                    containerColor = containerColor,
                    contentColor = contentColor,
                    disabledContainerColor = disabledContainerColor,
                    disabledContentColor = disabledContentColor,
                    shadowElevation = shadowElevation,
                    border = border,
                ),
            )
        }
    }

    @Test
    fun copy() {
        val original = appearance()
        benchmarkRule.measureRepeated {
            BlackHole.consume(original.copy())
        }
    }

    @Test
    fun copyAll() {
        val original = appearance()
        val shape = RoundedCornerShape(20.dp)
        val containerColor = Color(0xFFD0E8DC)
        val contentColor = Color(0xFF102E22)
        val disabledContainerColor = Color(0xFFD8DED9)
        val disabledContentColor = Color(0xFF58605A)
        val shadowElevation = 0.dp
        val border: BorderStroke? = BorderStroke(1.dp, Color(0xFF286B4C))
        benchmarkRule.measureRepeated {
            BlackHole.consume(
                original.copy(
                    shape = { shape },
                    containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
                    contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
                    shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
                    border = { border },
                ),
            )
        }
    }
}
