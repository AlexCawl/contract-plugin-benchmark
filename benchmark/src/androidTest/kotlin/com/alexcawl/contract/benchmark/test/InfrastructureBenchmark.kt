package com.alexcawl.contract.benchmark.test

import androidx.benchmark.BlackHole
import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import org.junit.Rule
import org.junit.Test

class InfrastructureBenchmark {
    @get:Rule
    val benchmarkRule = BenchmarkRule()

    @Test
    fun allocateObject() {
        benchmarkRule.measureRepeated {
            BlackHole.consume(Any())
        }
    }
}
