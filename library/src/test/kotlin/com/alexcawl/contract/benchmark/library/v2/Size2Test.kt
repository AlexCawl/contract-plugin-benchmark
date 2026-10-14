package com.alexcawl.contract.benchmark.library.v2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class Size2Test {
    private val arrangement: () -> Arrangement.Vertical = { Arrangement.Top }
    private val alignment: () -> Alignment.Horizontal = { Alignment.Start }

    @Test
    fun factoryReturnsImplementationWithCallbackEquality() {
        val value = Size2(arrangement, alignment)
        val implementation = Size2Impl(arrangement, alignment)

        assertTrue(value is Size2Impl)
        assertEquals(Arrangement.Top, value.contentArrangement())
        assertEquals(Alignment.Start, value.contentAlignment())
        assertEquals(value, implementation)
        assertEquals(implementation, value)
        assertEquals(31 * arrangement.hashCode() + alignment.hashCode(), value.hashCode())
        assertEquals(value.hashCode(), implementation.hashCode())
        assertEquals(value, value)
        assertFalse(value.equals(null))
        assertFalse(value.equals("Size2"))
    }

    @Test
    fun equalityComparesCallbacksRatherThanTheirResults() {
        val value = Size2(arrangement, alignment)
        val otherArrangement: () -> Arrangement.Vertical = { Arrangement.Top }
        val otherAlignment: () -> Alignment.Horizontal = { Alignment.Start }

        assertNotEquals(value, Size2(otherArrangement, alignment))
        assertNotEquals(value, Size2(arrangement, otherAlignment))
        assertEquals(value.contentArrangement(), otherArrangement())
        assertEquals(value.contentAlignment(), otherAlignment())
    }

    @Test
    fun copiesOverrideOnlySuppliedMethods() {
        val original = Size2(arrangement, alignment)
        val copied = original.copy(contentAlignment = { Alignment.End })

        assertTrue(copied is Size2Impl)
        assertEquals(Arrangement.Top, copied.contentArrangement())
        assertEquals(Alignment.End, copied.contentAlignment())
        assertEquals(Alignment.Start, original.contentAlignment())
        assertEquals(Arrangement.Bottom, copied.copy(contentArrangement = { Arrangement.Bottom }).contentArrangement())
    }

    @Test
    fun copyDefaultsStayBoundToCustomOriginalAcrossChains() {
        var currentAlignment: Alignment.Horizontal = Alignment.Start
        val original = object : Size2 {
            override fun contentArrangement(): Arrangement.Vertical =
                if (contentAlignment() == Alignment.Start) Arrangement.Top else Arrangement.Bottom

            override fun contentAlignment(): Alignment.Horizontal = currentAlignment
        }
        val copied = original.copy(contentAlignment = { Alignment.End })
        val chained = copied.copy(contentAlignment = { Alignment.CenterHorizontally })

        assertEquals(Alignment.End, copied.contentAlignment())
        assertEquals(Alignment.CenterHorizontally, chained.contentAlignment())
        assertEquals(Arrangement.Top, copied.contentArrangement())
        assertEquals(Arrangement.Top, chained.contentArrangement())
        currentAlignment = Alignment.End
        assertEquals(Arrangement.Bottom, copied.contentArrangement())
        assertEquals(Arrangement.Bottom, chained.contentArrangement())
    }

    @Test
    fun copyingMethodReferencesDiffersFromPreservingStoredCallbacks() {
        val original = Size2(arrangement, alignment)

        assertNotEquals(original, original.copy())
        assertEquals(original.copy(), original.copy())
        assertEquals(original, original.copy(contentArrangement = arrangement, contentAlignment = alignment))
    }

    @Test
    fun javaCopyOverloadUsesReceiverMethods() {
        val original = Size2(arrangement, alignment)
        val facade = Class.forName("com.alexcawl.contract.benchmark.library.v2.Size2Kt")
        val copied = facade.getMethod("copy", Size2::class.java).invoke(null, original) as Size2

        assertEquals(original.contentArrangement(), copied.contentArrangement())
        assertEquals(original.contentAlignment(), copied.contentAlignment())
    }
}
