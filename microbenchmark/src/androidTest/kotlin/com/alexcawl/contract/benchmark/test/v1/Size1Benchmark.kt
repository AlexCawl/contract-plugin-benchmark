package com.alexcawl.contract.benchmark.test.v1

import androidx.benchmark.BlackHole
import androidx.benchmark.ExperimentalBenchmarkConfigApi
import androidx.benchmark.MicrobenchmarkConfig
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.alexcawl.contract.benchmark.library.v1.Size1
import com.alexcawl.contract.benchmark.library.v1.copy
import com.alexcawl.contract.benchmark.test.AllocationBytesCapture
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalBenchmarkConfigApi::class)
class Size1Benchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule(
        if (InstrumentationRegistry.getArguments().getString("allocationBytes") == "true") {
            MicrobenchmarkConfig(metrics = listOf(AllocationBytesCapture()))
        } else {
            MicrobenchmarkConfig()
        },
    )

    @Suppress("NOTHING_TO_INLINE")
    private inline fun size(
        contentArrangement: Arrangement.Vertical = Arrangement.Top,
        contentAlignment: Alignment.Horizontal = Alignment.Start,
    ): Size1 = Size1(
        contentArrangement = { contentArrangement },
        contentAlignment = { contentAlignment },
    )

    @Test
    fun create() {
        val contentArrangement: Arrangement.Vertical = Arrangement.Top
        val contentAlignment: Alignment.Horizontal = Alignment.Start
        benchmarkRule.measureRepeated {
            BlackHole.consume(
                size(
                    contentArrangement = contentArrangement,
                    contentAlignment = contentAlignment,
                ),
            )
        }
    }

    @Test
    fun copy() {
        val original = size()
        benchmarkRule.measureRepeated {
            BlackHole.consume(original.copy())
        }
    }

    @Test
    fun copyAll() {
        val original = size()
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
}
