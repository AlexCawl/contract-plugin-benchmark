package com.alexcawl.contract.benchmark.library.v3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertSame
import org.junit.Test

class Size3Test {
    private val arrangement: Size3.() -> Arrangement.Vertical = { Arrangement.Top }
    private val alignment: Size3.() -> Alignment.Horizontal = { Alignment.Start }

    @Test
    fun factoryCallsCallbacksAndComparesTheirIdentity() {
        val original = Size3(arrangement, alignment)
        val equal = Size3(arrangement, alignment)

        assertEquals(Arrangement.Top, original.contentArrangement())
        assertEquals(Alignment.Start, original.contentAlignment())
        assertEquals(original, equal)
        assertEquals(equal, original)
        assertEquals(31 * arrangement.hashCode() + alignment.hashCode(), original.hashCode())
        assertEquals(original.hashCode(), equal.hashCode())
        assertFalse(original.equals(null))
        assertFalse(original.equals("Size3"))
    }

    @Test
    fun equalityAndHashingNeverEvaluateCallbacks() {
        val unreadArrangement: Size3.() -> Arrangement.Vertical = { error("Arrangement evaluated") }
        val unreadAlignment: Size3.() -> Alignment.Horizontal = { error("Alignment evaluated") }
        val original = Size3(unreadArrangement, unreadAlignment)
        val equal = Size3(unreadArrangement, unreadAlignment)

        assertEquals(original, original)
        assertEquals(original, equal)
        assertEquals(original.hashCode(), equal.hashCode())
        assertNotEquals(original, Size3(arrangement, unreadAlignment))
        assertNotEquals(original, Size3(unreadArrangement, alignment))
    }

    @Test
    fun contractSnapshotsAreFreshReferenceObjectsWithOriginalCallbacks() {
        val original = Size3(arrangement, alignment)
        val first = original.contract()
        val second = original.contract()

        assertNotSame(first, second)
        assertNotEquals(first, second)
        assertSame(arrangement, first.contentArrangement)
        assertSame(alignment, first.contentAlignment)
        assertSame(first.contentArrangement, second.contentArrangement)
        assertSame(first.contentAlignment, second.contentAlignment)
    }

    @Test
    fun unchangedCopiesAreDistinctEqualInstancesWithTheSameCallbacks() {
        val original = Size3(arrangement, alignment)
        val copied = original.copy()
        val chained = copied.copy(contentArrangement = null, contentAlignment = null)

        assertNotSame(original, copied)
        assertNotSame(copied, chained)
        assertEquals(original, copied)
        assertEquals(original, chained)
        assertEquals(original.hashCode(), copied.hashCode())
        assertSame(arrangement, chained.contract().contentArrangement)
        assertSame(alignment, chained.contract().contentAlignment)
    }

    @Test
    fun dependentCallbacksRebindThroughCopyChains() {
        val dependent: Size3.() -> Arrangement.Vertical = {
            if (contentAlignment() == Alignment.Start) Arrangement.Top else Arrangement.Bottom
        }
        val original = Size3(dependent, alignment)
        val copied = original.copy(contentAlignment = { Alignment.End })
        val chained = copied.copy(contentAlignment = { Alignment.Start })

        assertEquals(Arrangement.Top, original.contentArrangement())
        assertEquals(Alignment.End, copied.contentAlignment())
        assertEquals(Arrangement.Bottom, copied.contentArrangement())
        assertEquals(Arrangement.Top, chained.contentArrangement())
        assertSame(dependent, copied.contract().contentArrangement)
        assertSame(dependent, chained.contract().contentArrangement)
    }

    @Test
    fun rebindingChangesThisButPreservesExplicitlyCapturedObjects() {
        var seen: Size3? = null
        val captured = Size3(arrangement, alignment)
        val original = Size3(
            contentArrangement = {
                seen = this
                if (captured.contentAlignment() == Alignment.Start) Arrangement.Top else Arrangement.Bottom
            },
            contentAlignment = alignment,
        )
        val copied = original.copy(contentAlignment = { Alignment.End }).copy()

        assertEquals(Arrangement.Top, copied.contentArrangement())
        assertSame(copied, seen)
        assertEquals(Alignment.End, copied.contentAlignment())
        assertEquals(Alignment.Start, captured.contentAlignment())
    }

    @Test
    fun manualImplementationsProvideOneSnapshotPerCopy() {
        var snapshots = 0
        val dependent: Size3.() -> Arrangement.Vertical = {
            if (contentAlignment() == Alignment.Start) Arrangement.Top else Arrangement.Bottom
        }
        val manual = object : Size3 {
            override fun contentArrangement(): Arrangement.Vertical = dependent.invoke(this)
            override fun contentAlignment(): Alignment.Horizontal = alignment.invoke(this)
            override fun contract(): Size3.Contract {
                snapshots++
                return Size3.Contract(dependent, alignment)
            }
        }
        val copied = manual.copy(contentAlignment = { Alignment.End })

        assertEquals(1, snapshots)
        assertEquals(Arrangement.Top, manual.contentArrangement())
        assertEquals(Arrangement.Bottom, copied.contentArrangement())
        assertSame(dependent, copied.contract().contentArrangement)
        assertEquals(1, snapshots)
        assertNotEquals(copied, manual)
        val overridden = manual.copy(arrangement, alignment)
        assertEquals(2, snapshots)
        assertEquals(Arrangement.Top, overridden.contentArrangement())
    }
}
