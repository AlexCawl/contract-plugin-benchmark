package com.alexcawl.contract.benchmark.test

import android.os.Bundle
import android.os.Debug
import androidx.test.platform.app.InstrumentationRegistry
import java.io.File
import org.junit.Assert.assertTrue

internal fun dumpHeap(fileName: String) {
    val arguments = InstrumentationRegistry.getArguments()
    val instrumentation = InstrumentationRegistry.getInstrumentation()
    val directory = arguments.getString("additionalTestOutputDir")?.let(::File)
        ?: File(instrumentation.targetContext.getExternalFilesDir(null), "heap-dumps")
    check(directory.isDirectory || directory.mkdirs()) { "Cannot create $directory" }
    val output = File(directory, fileName)
    System.gc()
    System.runFinalization()
    Debug.dumpHprofData(output.absolutePath)
    assertTrue("Empty heap dump: $output", output.length() > 0)
    instrumentation.sendStatus(0, Bundle().apply { putString("heapDump", output.absolutePath) })
}
