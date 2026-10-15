package com.alexcawl.contract.benchmark.test

import android.util.Log
import androidx.benchmark.BlackHole
import androidx.benchmark.ExperimentalBenchmarkConfigApi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalBenchmarkConfigApi::class)
class AllocationBytesCaptureTest {
    @Test
    fun measuresBytesAndExcludesPausedAllocations() {
        val capture = AllocationBytesCapture()
        val output = longArrayOf(-1L, -1L)

        repeat(5) {
            measureBytes(capture, output) {
                repeat(ITERATIONS) { BlackHole.consume(ByteArray(1024)) }
            }
            measureBytes(capture, output) {
                repeat(ITERATIONS) { BlackHole.consume(ByteArray(2048)) }
            }
        }

        repeat(3) { round ->
            val empty = measureBytes(capture, output) {
                repeat(ITERATIONS) { BlackHole.consume(it) }
            }
            val small = measureBytes(capture, output) {
                repeat(ITERATIONS) { BlackHole.consume(ByteArray(1024)) }
            }
            val large = measureBytes(capture, output) {
                repeat(ITERATIONS) { BlackHole.consume(ByteArray(2048)) }
            }
            val paused = measureBytes(capture, output) {
                repeat(2) {
                    repeat(ITERATIONS / 2) { BlackHole.consume(ByteArray(1024)) }
                    capture.capturePaused()
                    repeat(ITERATIONS / 2) { BlackHole.consume(ByteArray(2048)) }
                    capture.captureResumed()
                }
            }

            Log.i(
                "AllocationBytesCapture",
                "Round ${round + 1}: empty=$empty, small=$small, large=$large, paused=$paused B/op",
            )
            assertTrue("Empty block allocated $empty B/op", empty in 0.0..1.0)
            assertTrue("1024-byte array allocated $small B/op", small > 0.0)
            assertTrue("2048-byte array allocated $large B/op", large > 0.0)
            assertEquals("Array size difference", 1024.0, large - small, 1024.0 * 0.01)
            assertEquals("Paused allocations must be excluded", small, paused, small * 0.01)
            assertEquals("Capture must respect output offset", -1L, output[0])
        }
    }

    private inline fun measureBytes(
        capture: AllocationBytesCapture,
        output: LongArray,
        block: () -> Unit,
    ): Double {
        capture.captureStart(0L)
        try {
            block()
        } finally {
            capture.captureStop(0L, output, 1)
        }
        return output[1] / ITERATIONS.toDouble()
    }

    companion object {
        private const val ITERATIONS = 10_000
    }
}
