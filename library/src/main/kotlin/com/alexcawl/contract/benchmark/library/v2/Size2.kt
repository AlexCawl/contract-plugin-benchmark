package com.alexcawl.contract.benchmark.library.v2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Stable
import androidx.compose.ui.Alignment

@Stable
interface Size2 {
    fun contentArrangement(): Arrangement.Vertical

    fun contentAlignment(): Alignment.Horizontal

    fun contract(): Contract

    class Contract(
        val contentArrangement: Size2.() -> Arrangement.Vertical,
        val contentAlignment: Size2.() -> Alignment.Horizontal,
    )
}

private class Size2Impl(
    private val contentArrangement: Size2.() -> Arrangement.Vertical,
    private val contentAlignment: Size2.() -> Alignment.Horizontal,
) : Size2 {
    override fun contentArrangement(): Arrangement.Vertical = contentArrangement.invoke(this)

    override fun contentAlignment(): Alignment.Horizontal = contentAlignment.invoke(this)

    override fun contract(): Size2.Contract = Size2.Contract(contentArrangement, contentAlignment)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size2Impl) return false
        return contentArrangement == other.contentArrangement && contentAlignment == other.contentAlignment
    }

    override fun hashCode(): Int = 31 * contentArrangement.hashCode() + contentAlignment.hashCode()
}

fun Size2(
    contentArrangement: Size2.() -> Arrangement.Vertical,
    contentAlignment: Size2.() -> Alignment.Horizontal,
): Size2 = Size2Impl(contentArrangement, contentAlignment)

fun Size2.copy(
    contentArrangement: (Size2.() -> Arrangement.Vertical)? = null,
    contentAlignment: (Size2.() -> Alignment.Horizontal)? = null,
): Size2 {
    val original = contract()
    return Size2(
        contentArrangement = contentArrangement ?: original.contentArrangement,
        contentAlignment = contentAlignment ?: original.contentAlignment,
    )
}
