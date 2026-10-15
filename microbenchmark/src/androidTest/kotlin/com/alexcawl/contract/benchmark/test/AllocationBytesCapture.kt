package com.alexcawl.contract.benchmark.test

import android.os.Debug
import androidx.benchmark.ExperimentalBenchmarkConfigApi
import androidx.benchmark.MetricCapture

@OptIn(ExperimentalBenchmarkConfigApi::class)
@Suppress("DEPRECATION")
internal class AllocationBytesCapture : MetricCapture(listOf("allocationBytes")) {
    private var pausedBytes = 0L
    private var pauseStartedBytes = 0L

    override fun captureStart(timeNs: Long) {
        pausedBytes = 0L
        Debug.startAllocCounting()
    }

    override fun captureStop(timeNs: Long, output: LongArray, offset: Int) {
        Debug.stopAllocCounting()
        val bytes = Debug.getGlobalAllocSize().toLong() - pausedBytes
        check(bytes >= 0L) { "Invalid allocation byte counter: $bytes" }
        output[offset] = bytes
    }

    override fun capturePaused() {
        pauseStartedBytes = Debug.getGlobalAllocSize().toLong()
    }

    override fun captureResumed() {
        pausedBytes += Debug.getGlobalAllocSize().toLong() - pauseStartedBytes
    }
}
