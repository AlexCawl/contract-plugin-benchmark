package com.alexcawl.contract.benchmark.test.v2

import android.os.Bundle
import android.os.Debug
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.test.platform.app.InstrumentationRegistry
import com.alexcawl.contract.benchmark.library.v2.Appearance2
import com.alexcawl.contract.benchmark.library.v2.Size2
import com.alexcawl.contract.benchmark.library.v2.copy
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class V2ContractHeapDumpTest(
    private val contract: String,
    private val operation: String,
) {
    @Test
    fun dump() {
        val arguments = InstrumentationRegistry.getArguments()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory = arguments.getString("additionalTestOutputDir")?.let(::File)
            ?: File(instrumentation.targetContext.getExternalFilesDir(null), "heap-dumps")
        check(directory.isDirectory || directory.mkdirs()) { "Cannot create $directory" }
        val output = File(directory, "V2_${contract}_${operation}.hprof")
        current = buildSample()
        try {
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
    private fun buildSample(): Any = when (contract) {
        "Size" -> {
            val original = run {
                val contentArrangement: Arrangement.Vertical = Arrangement.Top
                val contentAlignment: Alignment.Horizontal = Alignment.Start
                Size2(
                    contentArrangement = { contentArrangement },
                    contentAlignment = { contentAlignment },
                )
            }
            val contentArrangement: Arrangement.Vertical = Arrangement.spacedBy(8.dp)
            val contentAlignment: Alignment.Horizontal = Alignment.CenterHorizontally
            when (operation) {
                "create" -> original
                "copy" -> original.copy()
                "copyAll" -> original.copy(
                    contentArrangement = { contentArrangement },
                    contentAlignment = { contentAlignment },
                )
                "chain32" -> {
                    var sample = original
                    repeat(32) {
                        sample = sample.copy(
                            contentArrangement = { contentArrangement },
                            contentAlignment = { contentAlignment },
                        )
                    }
                    sample
                }
                else -> error("Unknown operation: $operation")
            }
        }
        "Appearance" -> {
            val original = run {
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
            val shape = RoundedCornerShape(20.dp)
            val containerColor = Color(0xFFD0E8DC)
            val contentColor = Color(0xFF102E22)
            val disabledContainerColor = Color(0xFFD8DED9)
            val disabledContentColor = Color(0xFF58605A)
            val shadowElevation = 0.dp
            val border: BorderStroke? = BorderStroke(1.dp, Color(0xFF286B4C))
            when (operation) {
                "create" -> original
                "copy" -> original.copy()
                "copyAll" -> original.copy(
                    shape = { shape },
                    containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
                    contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
                    shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
                    border = { border },
                )
                "chain32" -> {
                    var sample = original
                    repeat(32) {
                        sample = sample.copy(
                            shape = { shape },
                            containerColor = { enabled -> if (enabled) containerColor else disabledContainerColor },
                            contentColor = { enabled -> if (enabled) contentColor else disabledContentColor },
                            shadowElevation = { enabled, _ -> if (enabled) shadowElevation else 0.dp },
                            border = { border },
                        )
                    }
                    sample
                }
                else -> error("Unknown operation: $operation")
            }
        }
        else -> error("Unknown contract: $contract")
    }

    companion object {
        /** Keep only the final sample rooted; inspect current in the imported heap dump. */
        @JvmField
        var current: Any? = null

        @JvmStatic
        @Parameterized.Parameters(name = "{0}_{1}")
        fun samples(): List<Array<String>> {
            if (InstrumentationRegistry.getArguments().getString("heapDump") != "true") return emptyList()
            return listOf("Size", "Appearance").flatMap { contract ->
                listOf("create", "copy", "copyAll", "chain32").map { operation ->
                    arrayOf(contract, operation)
                }
            }
        }
    }
}
