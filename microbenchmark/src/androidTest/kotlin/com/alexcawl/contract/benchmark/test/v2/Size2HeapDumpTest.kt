package com.alexcawl.contract.benchmark.test.v2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.alexcawl.contract.benchmark.library.v2.Size2
import com.alexcawl.contract.benchmark.library.v2.copy
import com.alexcawl.contract.benchmark.test.dumpHeap
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

class Size2HeapDumpTest {
    @get:Rule
    val testName = TestName()

    @Before
    fun requireHeapDump() {
        assumeTrue(
            "Heap dumps require heapDump=true",
            InstrumentationRegistry.getArguments().getString("heapDump") == "true",
        )
    }

    @Test
    fun create() {
        current = size()
    }

    @Test
    fun copy() {
        current = size().copy()
    }

    @Test
    fun copyAll() {
        val original = size()
        val contentArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp)
        val contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
        current = original.copy(
            contentArrangement = { contentArrangement },
            contentAlignment = { contentAlignment },
        )
    }

    @Test
    fun chain32() {
        var sample = size()
        val contentArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp)
        val contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
        repeat(32) {
            sample = sample.copy(
                contentArrangement = { contentArrangement },
                contentAlignment = { contentAlignment },
            )
        }
        current = sample
    }

    @After
    fun dump() {
        if (current == null) return
        try {
            dumpHeap("V2_Size_${testName.methodName}.hprof")
        } finally {
            current = null
        }
    }

    @Suppress("NOTHING_TO_INLINE")
    private inline fun size(
        contentArrangement: Arrangement.Vertical = Arrangement.Top,
        contentAlignment: Alignment.Horizontal = Alignment.Start,
    ): Size2 = Size2(
        contentArrangement = { contentArrangement },
        contentAlignment = { contentAlignment },
    )

    companion object {
        @JvmField
        var current: Any? = null
    }
}
