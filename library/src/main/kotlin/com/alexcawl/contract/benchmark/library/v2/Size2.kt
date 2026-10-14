package com.alexcawl.contract.benchmark.library.v2

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

@Stable
interface Size2 {
    fun contentArrangement(): Arrangement.Vertical

    fun contentAlignment(): Alignment.Horizontal
}

object Size2Presets {
    val default: Size2
        @Composable
        get() = remember {
            Size2(
                contentArrangement = { Arrangement.Top },
                contentAlignment = { Alignment.Start },
            )
        }
}

class Size2Impl(
    private val contentArrangement: () -> Arrangement.Vertical,
    private val contentAlignment: () -> Alignment.Horizontal,
) : Size2 {
    override fun contentArrangement(): Arrangement.Vertical = contentArrangement.invoke()

    override fun contentAlignment(): Alignment.Horizontal = contentAlignment.invoke()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Size2Impl) return false
        return contentArrangement == other.contentArrangement && contentAlignment == other.contentAlignment
    }

    override fun hashCode(): Int = 31 * contentArrangement.hashCode() + contentAlignment.hashCode()
}

@JvmOverloads
fun Size2(
    contentArrangement: () -> Arrangement.Vertical,
    contentAlignment: () -> Alignment.Horizontal,
): Size2 = Size2Impl(contentArrangement, contentAlignment)

/** Method defaults remain bound to the original receiver. */
@JvmOverloads
fun Size2.copy(
    contentArrangement: () -> Arrangement.Vertical = this::contentArrangement,
    contentAlignment: () -> Alignment.Horizontal = this::contentAlignment,
): Size2 = Size2Impl(contentArrangement, contentAlignment)
