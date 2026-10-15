package com.alexcawl.contract.benchmark.test.v1

import android.os.Bundle
import android.os.Debug
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.alexcawl.contract.benchmark.library.v1.Appearance1
import com.alexcawl.contract.benchmark.library.v1.copy
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

class V1AppearanceHeapDumpTest {
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
        current = buildCreateSample()
        dump()
    }

    @Test
    fun copy() {
        current = buildCopySample()
        dump()
    }

    @Test
    fun copyAll() {
        current = buildCopyAllSample()
        dump()
    }

    @Test
    fun chain32() {
        current = buildChain32Sample()
        dump()
    }

    private fun dump() {
        try {
            val arguments = InstrumentationRegistry.getArguments()
            val instrumentation = InstrumentationRegistry.getInstrumentation()
            val directory = arguments.getString("additionalTestOutputDir")?.let(::File)
                ?: File(instrumentation.targetContext.getExternalFilesDir(null), "heap-dumps")
            check(directory.isDirectory || directory.mkdirs()) { "Cannot create $directory" }
            val output = File(directory, "V1_Appearance_${testName.methodName}.hprof")
            System.gc()
            System.runFinalization()
            Debug.dumpHprofData(output.absolutePath)
            assertTrue("Empty heap dump: $output", output.length() > 0)
            instrumentation.sendStatus(0, Bundle().apply { putString("heapDump", output.absolutePath) })
        } finally {
            current = null
        }
    }

    // Return before dumping so construction locals cannot root earlier contracts.
    private fun buildCreateSample(): Appearance1 {
        val shape = RoundedCornerShape(12.dp)
        val containerColor = Color(0xFFE8DEF8)
        val contentColor = Color(0xFF1D192B)
        val disabledContainerColor = Color(0xFFE0E0E0)
        val disabledContentColor = Color(0xFF757575)
        val shadowElevation = 0.dp
        val border: BorderStroke? = null
        return Appearance1(
            shape = { shape },
            containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
            contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
            shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
            border = { border },
        )
    }

    private fun buildCopySample(): Appearance1 = buildCreateSample().copy()

    private fun buildCopyAllSample(): Appearance1 {
        val original = buildCreateSample()
        val shape = RoundedCornerShape(20.dp)
        val containerColor = Color(0xFFD0E8DC)
        val contentColor = Color(0xFF102E22)
        val disabledContainerColor = Color(0xFFD8DED9)
        val disabledContentColor = Color(0xFF58605A)
        val shadowElevation = 0.dp
        val border: BorderStroke? = BorderStroke(1.dp, Color(0xFF286B4C))
        return original.copy(
            shape = { shape },
            containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
            contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
            shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
            border = { border },
        )
    }

    private fun buildChain32Sample(): Appearance1 {
        var sample = buildCreateSample()
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
        return sample
    }

    companion object {
        /** Keep only the final sample rooted; inspect current in the imported heap dump. */
        @JvmField
        var current: Any? = null
    }
}
