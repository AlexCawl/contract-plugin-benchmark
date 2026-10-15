package com.alexcawl.contract.benchmark.library.v3

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

@Stable
interface Size3 {
    fun contentArrangement(): Arrangement.Vertical

    fun contentAlignment(): Alignment.Horizontal

    fun contract(): Contract

    class Contract(
        val contentArrangement: Size3.() -> Arrangement.Vertical,
        val contentAlignment: Size3.() -> Alignment.Horizontal,
    )
}

object Size3Presets {
    val default: Size3
        @Composable
        get() = remember {
            Size3(
                contentArrangement = { Arrangement.Top },
                contentAlignment = { Alignment.Start },
            )
        }
}

private class Size3Impl(
    private val contentArrangement: Size3.() -> Arrangement.Vertical,
    private val contentAlignment: Size3.() -> Alignment.Horizontal,
) : Size3 {
    override fun contentArrangement(): Arrangement.Vertical = contentArrangement.invoke(this)

    override fun contentAlignment(): Alignment.Horizontal = contentAlignment.invoke(this)

    override fun contract(): Size3.Contract = Size3.Contract(contentArrangement, contentAlignment)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size3Impl) return false
        return contentArrangement == other.contentArrangement && contentAlignment == other.contentAlignment
    }

    override fun hashCode(): Int = 31 * contentArrangement.hashCode() + contentAlignment.hashCode()
}

fun Size3(
    contentArrangement: Size3.() -> Arrangement.Vertical,
    contentAlignment: Size3.() -> Alignment.Horizontal,
): Size3 = Size3Impl(contentArrangement, contentAlignment)

fun Size3.copy(
    contentArrangement: (Size3.() -> Arrangement.Vertical)? = null,
    contentAlignment: (Size3.() -> Alignment.Horizontal)? = null,
): Size3 {
    val original = contract()
    return Size3(
        contentArrangement = contentArrangement ?: original.contentArrangement,
        contentAlignment = contentAlignment ?: original.contentAlignment,
    )
}
