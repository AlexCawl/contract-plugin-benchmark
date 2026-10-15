package com.alexcawl.contract.benchmark.macro

import android.content.ComponentName
import android.content.Intent
import androidx.benchmark.macro.CompilationMode
import androidx.benchmark.macro.FrameTimingMetric
import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.benchmark.macro.StartupMode
import androidx.benchmark.macro.StartupTimingMetric
import androidx.benchmark.macro.junit4.MacrobenchmarkRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Direction
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

class Activity2Macrobenchmark {
    @get:Rule
    val benchmarkRule = MacrobenchmarkRule()

    private fun activityIntent() = Intent().apply {
        component = ComponentName(PACKAGE_NAME, "$PACKAGE_NAME.v2.Activity2")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }

    @Test
    fun coldStartup() = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(StartupTimingMetric()),
        compilationMode = CompilationMode.Full(),
        startupMode = StartupMode.COLD,
        iterations = 10,
        setupBlock = { pressHome() },
    ) {
        startActivityAndWait(activityIntent())
    }

    @Test
    fun scroll() = frames {
        val list = checkNotNull(device.wait(Until.findObject(By.res("contract_list")), 5_000))
        list.setGestureMarginPercentage(0.2f)
        repeat(5) {
            list.fling(Direction.DOWN, 2_500)
            device.waitForIdle()
        }
        repeat(5) {
            list.fling(Direction.UP, 2_500)
            device.waitForIdle()
        }
    }

    @Test
    fun copyAllUpdates() = frames {
        repeat(10) { revision ->
            checkNotNull(device.findObject(By.res("change_appearance"))).click()
            check(device.wait(Until.hasObject(By.text("Оформление: ${revision + 1}")), 5_000)) {
                "Appearance update ${revision + 1} did not finish"
            }
            device.waitForIdle()
        }
    }

    private fun frames(block: MacrobenchmarkScope.() -> Unit) = benchmarkRule.measureRepeated(
        packageName = PACKAGE_NAME,
        metrics = listOf(FrameTimingMetric()),
        compilationMode = CompilationMode.Full(),
        iterations = 10,
        setupBlock = {
            killProcess()
            pressHome()
            startActivityAndWait(activityIntent())
            check(device.wait(Until.hasObject(By.res("contract_list")), 5_000))
            device.waitForIdle()
        },
        measureBlock = block,
    )

    companion object {
        private const val PACKAGE_NAME = "com.alexcawl.contract.benchmark.app"
    }
}
