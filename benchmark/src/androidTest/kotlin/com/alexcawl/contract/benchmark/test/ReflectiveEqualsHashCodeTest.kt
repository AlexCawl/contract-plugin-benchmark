package com.alexcawl.contract.benchmark.test

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.alexcawl.contract.benchmark.library.v1.ActionCardAppearance
import com.alexcawl.contract.benchmark.library.v1.ActionCardSize
import com.alexcawl.contract.benchmark.library.v1.ReflectiveEqualsHashCode
import com.alexcawl.contract.benchmark.library.v1.copy
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReflectiveEqualsHashCodeTest {
    @Test
    fun comparesAllPrimitiveKindsAndObjectCaptures() {
        val original = Fields()
        val same = Fields()
        assertEquals(original, same)
        assertEquals(original.hashCode(), same.hashCode())
        listOf(
            Fields(boolean = false),
            Fields(char = 'b'),
            Fields(byte = 2),
            Fields(short = 2),
            Fields(int = 2),
            Fields(long = 2),
            Fields(float = 2f),
            Fields(double = 2.0),
            Fields(reference = "other"),
        ).forEach { assertNotEquals(original, it) }
        assertFalse(original.equals(null))
        assertFalse(original.equals(Any()))
    }

    @Test
    fun preservesV1NullFieldBehavior() {
        val first = Fields(reference = null)
        val second = Fields(reference = null)
        assertTrue(first == first)
        assertFalse(first == second)
        assertEquals(first.hashCode(), second.hashCode())
    }

    @Test
    fun factoriesCompareCapturesFromTheSameCallSite() {
        val first = sizeWithHeight(80)
        val same = sizeWithHeight(80)
        assertEquals(first, same)
        assertEquals(first.hashCode(), same.hashCode())
        assertNotEquals(first, sizeWithHeight(96))

        val appearance = appearanceWithAlpha(0.5f)
        val sameAppearance = appearanceWithAlpha(0.5f)
        assertEquals(appearance, sameAppearance)
        assertEquals(appearance.hashCode(), sameAppearance.hashCode())
        assertNotEquals(appearance, appearanceWithAlpha(0.8f))
        assertNotEquals(first, appearance)
    }

    @Test
    fun copiesCompareOriginalAndOverrideCaptures() {
        val original = sizeWithHeight(80)
        val first = sizeCopy(original, 8)
        val same = sizeCopy(original, 8)
        assertEquals(first, same)
        assertEquals(first.hashCode(), same.hashCode())
        assertNotEquals(first, sizeCopy(original, 12))
        assertNotEquals(first, sizeCopy(sizeWithHeight(96), 8))
    }
}

private fun sizeWithHeight(height: Int): ActionCardSize = ActionCardSize(
    minHeight = { height.dp },
)

private fun appearanceWithAlpha(alpha: Float): ActionCardAppearance = ActionCardAppearance(
    leadingColor = { Color.Red.copy(alpha = alpha) },
)

private fun sizeCopy(original: ActionCardSize, spacing: Int): ActionCardSize = original.copy(
    horizontalSpacing = { spacing.dp },
)

private class Fields(
    val boolean: Boolean = true,
    val char: Char = 'a',
    val byte: Byte = 1,
    val short: Short = 1,
    val int: Int = 1,
    val long: Long = 1,
    val float: Float = 1f,
    val double: Double = 1.0,
    val reference: String? = "value",
) : ReflectiveEqualsHashCode()
