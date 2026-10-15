package com.alexcawl.contract.benchmark.test.v2

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.alexcawl.contract.benchmark.library.v2.Appearance2
import com.alexcawl.contract.benchmark.library.v2.copy
import com.alexcawl.contract.benchmark.test.dumpHeap
import org.junit.After
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

class Appearance2HeapDumpTest {
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
        current = appearance()
    }

    @Test
    fun copy() {
        current = appearance().copy()
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
        current = original.copy(
            shape = { shape },
            containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
            contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
            shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
            border = { border },
        )
    }

    @Test
    fun chain32() {
        var sample = appearance()
        val shape = RoundedCornerShape(20.dp)
        val containerColor = Color(0xFFD0E8DC)
        val contentColor = Color(0xFF102E22)
        val disabledContainerColor = Color(0xFFD8DED9)
        val disabledContentColor = Color(0xFF58605A)
        val shadowElevation = 0.dp
        val border: BorderStroke? = BorderStroke(1.dp, Color(0xFF286B4C))
        repeat(32) {
            sample = sample.copy(
                shape = { shape },
                containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
                contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
                shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
                border = { border },
            )
        }
        current = sample
    }

    @After
    fun dump() {
        if (current == null) return
        try {
            dumpHeap("V2_Appearance_${testName.methodName}.hprof")
        } finally {
            current = null
        }
    }

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

    companion object {
        @JvmField
        var current: Any? = null
    }
}
