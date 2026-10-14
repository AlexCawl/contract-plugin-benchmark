package com.alexcawl.contract.benchmark.library.v2

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class Appearance2Test {
    private val shape: () -> Shape = { RectangleShape }
    private val container: (Boolean) -> Color = { if (it) Color.Red else Color.Gray }
    private val content: (Boolean) -> Color = { if (it) Color.White else Color.Black }
    private val elevation: (Boolean, Interaction?) -> Dp = { enabled, interaction ->
        if (!enabled) 2.dp else if (interaction == null) 3.dp else 4.dp
    }
    private val border: () -> BorderStroke? = { null }

    @Test
    fun factoryReturnsImplementationWithCallbackEqualityAndHashing() {
        val value = Appearance2(shape, container, content, elevation, border)
        val implementation = Appearance2Impl(shape, container, content, elevation, border)

        assertTrue(value is Appearance2Impl)
        assertEquals(RectangleShape, value.shape())
        assertEquals(Color.Red, value.containerColor(true))
        assertEquals(Color.Gray, value.containerColor(false))
        assertEquals(Color.White, value.contentColor(true))
        assertEquals(Color.Black, value.contentColor(false))
        assertNull(value.border())
        assertEquals(value, implementation)
        assertEquals(implementation, value)
        val expectedHash = listOf(shape, container, content, elevation, border)
            .fold(0) { hash, callback -> 31 * hash + callback.hashCode() }
        assertEquals(expectedHash, value.hashCode())
        assertEquals(value.hashCode(), implementation.hashCode())
        assertEquals(value, value)
        assertFalse(value.equals(null))
        assertFalse(value.equals("Appearance2"))
    }

    @Test
    fun everyCallbackParticipatesInEquality() {
        val original = Appearance2(shape, container, content, elevation, border)
        val different = listOf(
            Appearance2({ RectangleShape }, container, content, elevation, border),
            Appearance2(shape, { if (it) Color.Red else Color.Gray }, content, elevation, border),
            Appearance2(shape, container, { if (it) Color.White else Color.Black }, elevation, border),
            Appearance2(shape, container, content, { _, _ -> 3.dp }, border),
            Appearance2(shape, container, content, elevation, { null }),
        )

        different.forEach { assertNotEquals(original, it) }
    }

    @Test
    fun interfaceAndFactoryDefaultsMatchFilledMaterialCardTargets() {
        val factory = Appearance2(shape, container, content)
        val manual = object : Appearance2 {
            override fun shape(): Shape = RectangleShape
            override fun containerColor(enabled: Boolean): Color = container(enabled)
            override fun contentColor(enabled: Boolean): Color = content(enabled)
        }
        val hover = HoverInteraction.Enter()
        val drag = DragInteraction.Start()
        val focus = FocusInteraction.Focus()
        val press = PressInteraction.Press(Offset.Zero)
        val targets = listOf(
            null to 0.dp,
            hover to 1.dp,
            drag to 6.dp,
            focus to 0.dp,
            press to 0.dp,
            HoverInteraction.Exit(hover) to 0.dp,
            DragInteraction.Stop(drag) to 0.dp,
            DragInteraction.Cancel(drag) to 0.dp,
            FocusInteraction.Unfocus(focus) to 0.dp,
            PressInteraction.Release(press) to 0.dp,
            PressInteraction.Cancel(press) to 0.dp,
        )

        for (appearance in listOf(factory, manual)) {
            assertNull(appearance.border())
            for ((interaction, expected) in targets) {
                assertEquals(expected, appearance.shadowElevation(true, interaction))
                assertEquals(0.dp, appearance.shadowElevation(false, interaction))
            }
        }
    }

    @Test
    fun implementationAndCopyForwardEnabledAndNullableInteraction() {
        var receivedEnabled = true
        var receivedInteraction: Interaction? = null
        val value = Appearance2(
            shape, container, content,
            shadowElevation = { enabled, interaction ->
                receivedEnabled = enabled
                receivedInteraction = interaction
                9.dp
            },
        )
        val interaction = HoverInteraction.Enter()

        assertEquals(9.dp, value.shadowElevation(false, interaction))
        assertFalse(receivedEnabled)
        assertSame(interaction, receivedInteraction)
        assertEquals(9.dp, value.copy().shadowElevation(true, null))
        assertTrue(receivedEnabled)
        assertNull(receivedInteraction)
    }

    @Test
    fun copiesOverrideCallbacksAndCanPreserveExplicitCallbackEquality() {
        val original = Appearance2(shape, container, content, elevation, border)
        val changedShape = RoundedCornerShape(12.dp)
        val changedBorder = BorderStroke(1.dp, Color.Green)
        val copied = original.copy(
            shape = { changedShape },
            containerColor = { Color.Blue },
            contentColor = { Color.Yellow },
            shadowElevation = { _, _ -> 8.dp },
            border = { changedBorder },
        )

        assertTrue(copied is Appearance2Impl)
        assertEquals(changedShape, copied.shape())
        assertEquals(Color.Blue, copied.containerColor(true))
        assertEquals(Color.Yellow, copied.contentColor(false))
        assertEquals(8.dp, copied.shadowElevation(false, null))
        assertEquals(changedBorder, copied.border())
        assertEquals(RectangleShape, original.shape())
        assertNotEquals(original, original.copy())
        assertEquals(original.copy(), original.copy())
        assertEquals(original, original.copy(shape, container, content, elevation, border))
    }

    @Test
    fun chainedCopiesKeepCustomOriginalMethodDependencies() {
        var currentColor = Color.Red
        val original = object : Appearance2 {
            override fun shape(): Shape = RectangleShape
            override fun containerColor(enabled: Boolean): Color = currentColor
            override fun contentColor(enabled: Boolean): Color = containerColor(enabled)
            override fun border(): BorderStroke = BorderStroke(1.dp, containerColor(true))
        }
        val copied = original.copy(containerColor = { Color.Blue })
        val chained = copied.copy(shape = { RoundedCornerShape(4.dp) })

        assertEquals(Color.Blue, copied.containerColor(true))
        assertEquals(Color.Blue, chained.containerColor(true))
        assertEquals(Color.Red, copied.contentColor(true))
        assertEquals(Color.Red, chained.contentColor(true))
        assertEquals(BorderStroke(1.dp, Color.Red), chained.border())
        currentColor = Color.Green
        assertEquals(Color.Green, copied.contentColor(true))
        assertEquals(Color.Green, chained.contentColor(true))
        assertEquals(BorderStroke(1.dp, Color.Green), chained.border())
    }

    @Test
    fun javaOverloadsSupplyFactoryAndCopyDefaults() {
        val facade = Class.forName("com.alexcawl.contract.benchmark.library.v2.Appearance2Kt")
        val factory = facade.getMethod(
            "Appearance2", Function0::class.java, Function1::class.java, Function1::class.java,
        )
        val original = factory.invoke(null, shape, container, content) as Appearance2
        val copied = facade.getMethod("copy", Appearance2::class.java).invoke(null, original) as Appearance2

        assertEquals(Color.Red, copied.containerColor(true))
        assertEquals(Color.Gray, copied.containerColor(false))
        assertNull(copied.border())
        assertEquals(1.dp, copied.shadowElevation(true, HoverInteraction.Enter()))
        assertEquals(0.dp, copied.shadowElevation(false, DragInteraction.Start()))
    }
}
