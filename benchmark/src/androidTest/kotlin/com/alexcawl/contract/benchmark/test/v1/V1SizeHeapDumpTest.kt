package com.alexcawl.contract.benchmark.test.v1

import android.os.Bundle
import android.os.Debug
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.alexcawl.contract.benchmark.library.v1.Size1
import com.alexcawl.contract.benchmark.library.v1.copy
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestName

class V1SizeHeapDumpTest {
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
            val output = File(directory, "V1_Size_${testName.methodName}.hprof")
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
    private fun buildCreateSample(): Size1 {
        val contentArrangement: Arrangement.Vertical = Arrangement.Top
        val contentAlignment: Alignment.Horizontal = Alignment.Start
        return Size1(
            contentArrangement = { contentArrangement },
            contentAlignment = { contentAlignment },
        )
    }

    private fun buildCopySample(): Size1 = buildCreateSample().copy()

    private fun buildCopyAllSample(): Size1 {
        val original = buildCreateSample()
        val contentArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp)
        val contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
        return original.copy(
            contentArrangement = { contentArrangement },
            contentAlignment = { contentAlignment },
        )
    }

    private fun buildChain32Sample(): Size1 {
        var sample = buildCreateSample()
        val contentArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp)
        val contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
        repeat(32) {
            sample = sample.copy(
                contentArrangement = { contentArrangement },
                contentAlignment = { contentAlignment },
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
