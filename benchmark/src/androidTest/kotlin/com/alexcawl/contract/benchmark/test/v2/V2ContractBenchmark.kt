package com.alexcawl.contract.benchmark.test.v2

import androidx.benchmark.BlackHole
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.benchmark.library.v2.Appearance2
import com.alexcawl.contract.benchmark.library.v2.Size2
import com.alexcawl.contract.benchmark.library.v2.copy
import org.junit.Rule
import org.junit.Test

class V2ContractBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val size = run {
        val contentArrangement: Arrangement.Vertical = Arrangement.Top
        val contentAlignment: Alignment.Horizontal = Alignment.Start
        Size2(
            contentArrangement = { contentArrangement },
            contentAlignment = { contentAlignment },
        )
    }

    private val appearance = run {
        val shape = RoundedCornerShape(12.dp)
        val containerColor = Color(0xFFE8DEF8)
        val contentColor = Color(0xFF1D192B)
        val disabledContainerColor = Color(0xFFE0E0E0)
        val disabledContentColor = Color(0xFF757575)
        val shadowElevation = 0.dp
        val border: BorderStroke? = null
        Appearance2(
            shape = { shape },
            containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
            contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
            shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
            border = { border },
        )
    }

    @Test
    fun size_create() {
        val contentArrangement: Arrangement.Vertical = Arrangement.Top
        val contentAlignment: Alignment.Horizontal = Alignment.Start
        benchmarkRule.measureRepeated {
            BlackHole.consume(
                Size2(
                    contentArrangement = { contentArrangement },
                    contentAlignment = { contentAlignment },
                ),
            )
        }
    }

    @Test
    fun size_copy() {
        val original = size
        benchmarkRule.measureRepeated {
            BlackHole.consume(original.copy())
        }
    }

    @Test
    fun size_copyAll() {
        val original = size
        val contentArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp)
        val contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
        benchmarkRule.measureRepeated {
            BlackHole.consume(
                original.copy(
                    contentArrangement = { contentArrangement },
                    contentAlignment = { contentAlignment },
                ),
            )
        }
    }

    @Test
    fun appearance_create() {
        val shape = RoundedCornerShape(12.dp)
        val containerColor = Color(0xFFE8DEF8)
        val contentColor = Color(0xFF1D192B)
        val disabledContainerColor = Color(0xFFE0E0E0)
        val disabledContentColor = Color(0xFF757575)
        val shadowElevation = 0.dp
        val border: BorderStroke? = null
        benchmarkRule.measureRepeated {
            BlackHole.consume(
                Appearance2(
                    shape = { shape },
                    containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
                    contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
                    shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
                    border = { border },
                ),
            )
        }
    }

    @Test
    fun appearance_copy() {
        val original = appearance
        benchmarkRule.measureRepeated {
            BlackHole.consume(original.copy())
        }
    }

    @Test
    fun appearance_copyAll() {
        val original = appearance
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
