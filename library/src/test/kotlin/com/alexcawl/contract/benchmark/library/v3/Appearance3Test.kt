package com.alexcawl.contract.benchmark.library.v3

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.FocusInteraction
import androidx.compose.foundation.interaction.HoverInteraction
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class Appearance3Test {
    private val shape: Appearance3.() -> Shape = { RectangleShape }
    private val container: Appearance3.(Boolean) -> Color = { if (it) Color.Red else Color.Gray }
    private val content: Appearance3.(Boolean) -> Color = { if (it) Color.White else Color.Black }
    private val elevation: Appearance3.(Boolean, Interaction?) -> Dp = { enabled, interaction ->
        if (!enabled) 2.dp else if (interaction == null) 3.dp else 4.dp
    }
    private val border: Appearance3.() -> BorderStroke? = { null }

    @Test
    fun factoryCallsCallbacksAndIncludesEveryCallbackInEqualityAndHashing() {
        val original = Appearance3(shape, container, content, elevation, border)
        val equal = Appearance3(shape, container, content, elevation, border)

        assertEquals(RectangleShape, original.shape())
        assertEquals(Color.Red, original.containerColor(true))
        assertEquals(Color.Gray, original.containerColor(false))
        assertEquals(Color.White, original.contentColor(true))
        assertEquals(Color.Black, original.contentColor(false))
        assertEquals(3.dp, original.shadowElevation(true, null))
        assertNull(original.border())
        assertEquals(original, equal)
        assertEquals(equal, original)
        val expectedHash = listOf(shape, container, content, elevation, border)
            .fold(0) { hash, callback -> 31 * hash + callback.hashCode() }
        assertEquals(expectedHash, original.hashCode())
        assertEquals(original.hashCode(), equal.hashCode())
        assertFalse(original.equals(null))
        assertFalse(original.equals("Appearance3"))
        val different = listOf(
            Appearance3({ RectangleShape }, container, content, elevation, border),
            Appearance3(shape, { if (it) Color.Red else Color.Gray }, content, elevation, border),
            Appearance3(shape, container, { if (it) Color.White else Color.Black }, elevation, border),
            Appearance3(shape, container, content, { _, _ -> 3.dp }, border),
            Appearance3(shape, container, content, elevation, { null }),
        )
        different.forEach { assertNotEquals(original, it) }
    }

    @Test
    fun absentCallbacksUseInterfaceDefaultsAndHashAsZero() {
        val original = Appearance3(shape, container, content)
        val callbacks = original.contract()
        val hover = HoverInteraction.Enter()
        val drag = DragInteraction.Start()
        val press = PressInteraction.Press(Offset.Zero)
        val focus = FocusInteraction.Focus()
        val targets = listOf(
            null to 0.dp,
            hover to 1.dp,
            drag to 6.dp,
            press to 0.dp,
            focus to 0.dp,
            HoverInteraction.Exit(hover) to 0.dp,
            DragInteraction.Stop(drag) to 0.dp,
            DragInteraction.Cancel(drag) to 0.dp,
            PressInteraction.Release(press) to 0.dp,
            PressInteraction.Cancel(press) to 0.dp,
            FocusInteraction.Unfocus(focus) to 0.dp,
        )

        assertNull(callbacks.shadowElevation)
        assertNull(callbacks.border)
        assertNull(original.border())
        assertEquals(Appearance3(shape, container, content), original)
        val expectedHash = listOf(shape, container, content, null, null)
            .fold(0) { hash, callback -> 31 * hash + (callback?.hashCode() ?: 0) }
        assertEquals(expectedHash, original.hashCode())
        for ((interaction, target) in targets) {
            assertEquals(target, original.shadowElevation(true, interaction))
            assertEquals(0.dp, original.shadowElevation(false, interaction))
        }
        assertEquals(original, original.copy())
        assertNull(original.copy().contract().shadowElevation)
        assertNull(original.copy().contract().border)
    }

    @Test
    fun snapshotsAndUnchangedCopiesPreserveCallbackReferences() {
        val original = Appearance3(shape, container, content, elevation, border)
        val first = original.contract()
        val second = original.contract()
        val copied = original.copy()
        val transferred = copied.contract()

        assertNotSame(first, second)
        assertNotEquals(first, second)
        assertNotSame(original, copied)
        assertEquals(original, copied)
        assertEquals(original.hashCode(), copied.hashCode())
        assertSame(shape, first.shape)
        assertSame(container, first.containerColor)
        assertSame(content, first.contentColor)
        assertSame(elevation, first.shadowElevation)
        assertSame(border, first.border)
        assertSame(first.shape, transferred.shape)
        assertSame(first.containerColor, transferred.containerColor)
        assertSame(first.contentColor, transferred.contentColor)
        assertSame(first.shadowElevation, transferred.shadowElevation)
        assertSame(first.border, transferred.border)
    }

    @Test
    fun dependenciesInColorsBorderAndElevationRebindThroughCopies() {
        val original = Appearance3(
            shape, container,
            contentColor = { enabled -> containerColor(enabled) },
            shadowElevation = { enabled, _ ->
                if (!enabled) 0.dp else if (containerColor(true) == Color.Blue) 8.dp else 1.dp
            },
            border = { BorderStroke(1.dp, contentColor(true)) },
        )
        val copied = original.copy(containerColor = { if (it) Color.Blue else Color.Black })
        val chained = copied.copy(containerColor = { Color.Green })

        assertEquals(Color.Red, original.contentColor(true))
        assertEquals(Color.Blue, copied.contentColor(true))
        assertEquals(Color.Black, copied.contentColor(false))
        assertEquals(BorderStroke(1.dp, Color.Blue), copied.border())
        assertEquals(8.dp, copied.shadowElevation(true, null))
        assertEquals(0.dp, copied.shadowElevation(false, null))
        assertEquals(Color.Green, chained.contentColor(true))
        assertEquals(BorderStroke(1.dp, Color.Green), chained.border())
        assertEquals(1.dp, chained.shadowElevation(true, null))
        assertEquals(Color.Red, original.contentColor(true))
        assertSame(original.contract().contentColor, chained.contract().contentColor)
    }

    @Test
    fun overridesReceiveCurrentCopyEnabledAndNullableInteraction() {
        var seen: Appearance3? = null
        var receivedEnabled = true
        var receivedInteraction: Interaction? = null
        val original = Appearance3(
            shape, container, content,
            shadowElevation = { enabled, interaction ->
                seen = this
                receivedEnabled = enabled
                receivedInteraction = interaction
                9.dp
            },
        )
        val copied = original.copy().copy()
        val interaction = HoverInteraction.Enter()

        assertEquals(9.dp, copied.shadowElevation(false, interaction))
        assertSame(copied, seen)
        assertFalse(receivedEnabled)
        assertSame(interaction, receivedInteraction)
        assertEquals(9.dp, original.shadowElevation(true, null))
        assertSame(original, seen)
        assertTrue(receivedEnabled)
        assertNull(receivedInteraction)
        val changedShape = RoundedCornerShape(12.dp)
        assertEquals(changedShape, copied.copy(shape = { changedShape }).shape())
    }

    @Test
    fun nullCopyArgumentsPreserveOverridesIncludingCallbacksReturningNull() {
        val original = Appearance3(
            shape, container, content, elevation,
            border = { BorderStroke(1.dp, Color.Blue) },
        )
        val cleared = original.copy(border = { null })
        val preserved = cleared.copy(
            shape = null, containerColor = null, contentColor = null, shadowElevation = null, border = null,
        )

        assertNull(cleared.border())
        assertNotNull(cleared.contract().border)
        assertSame(cleared.contract().border, preserved.contract().border)
        assertSame(elevation, preserved.contract().shadowElevation)
        assertNull(preserved.border())
        assertEquals(2.dp, preserved.shadowElevation(false, null))
        assertEquals(cleared, preserved)
        assertNotEquals(Appearance3(shape, container, content), Appearance3(shape, container, content, border = border))
    }

    @Test
    fun equalityDoesNotInvokeRequiredOrOptionalCallbacks() {
        val unreadShape: Appearance3.() -> Shape = { error("Shape evaluated") }
        val unreadColor: Appearance3.(Boolean) -> Color = { error("Color evaluated") }
        val unreadElevation: Appearance3.(Boolean, Interaction?) -> Dp = { _, _ -> error("Elevation evaluated") }
        val unreadBorder: Appearance3.() -> BorderStroke? = { error("Border evaluated") }
        val original = Appearance3(unreadShape, unreadColor, unreadColor, unreadElevation, unreadBorder)
        val equal = Appearance3(unreadShape, unreadColor, unreadColor, unreadElevation, unreadBorder)

        assertEquals(original, equal)
        assertEquals(original.hashCode(), equal.hashCode())
        assertNotEquals(original, Appearance3(unreadShape, unreadColor, unreadColor))
    }

    @Test
    fun manualImplementationsSupplyOneSnapshotAndRebindItsCallbacks() {
        var snapshots = 0
        val dependent: Appearance3.(Boolean) -> Color = { enabled -> containerColor(enabled) }
        val manual = object : Appearance3 {
            override fun shape(): Shape = shape.invoke(this)
            override fun containerColor(enabled: Boolean): Color = container.invoke(this, enabled)
            override fun contentColor(enabled: Boolean): Color = dependent.invoke(this, enabled)
            override fun contract(): Appearance3.Contract {
                snapshots++
                return Appearance3.Contract(shape, container, dependent)
            }
        }
        val copied = manual.copy(containerColor = { Color.Blue })

        assertEquals(1, snapshots)
        assertEquals(Color.Red, manual.contentColor(true))
        assertEquals(Color.Blue, copied.contentColor(true))
        assertSame(dependent, copied.contract().contentColor)
        assertNull(copied.contract().shadowElevation)
        assertNull(copied.contract().border)
        assertEquals(1.dp, copied.shadowElevation(true, HoverInteraction.Enter()))
        assertNull(copied.border())
        assertEquals(1, snapshots)
        assertNotEquals(copied, manual)
        val overridden = manual.copy(shape, container, content, elevation, border)
        assertEquals(2, snapshots)
        assertEquals(Color.White, overridden.contentColor(true))
    }
}
