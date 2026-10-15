package com.alexcawl.contract.benchmark.library.v1

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.annotation.RememberInComposition
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment

@Stable
interface Size1 {

    @Composable
    fun contentArrangement(): Arrangement.Vertical

    @Composable
    fun contentAlignment(): Alignment.Horizontal
}

inline fun Size1(
    crossinline contentArrangement: @Composable Size1.() -> Arrangement.Vertical,
    crossinline contentAlignment: @Composable Size1.() -> Alignment.Horizontal,
): Size1 = object : ReflectiveEqualsHashCode(), Size1 {
    @Composable
    override fun contentArrangement(): Arrangement.Vertical = contentArrangement.invoke(this)

    @Composable
    override fun contentAlignment(): Alignment.Horizontal = contentAlignment.invoke(this)
}

interface Size1Wrapper : Size1 {
    val wrapped: Size1
}

@RememberInComposition
inline fun Size1.copy(
    crossinline contentArrangement: @Composable Size1Wrapper.() -> Arrangement.Vertical = { wrapped.contentArrangement() },
    crossinline contentAlignment: @Composable Size1Wrapper.() -> Alignment.Horizontal = { wrapped.contentAlignment() },
): Size1 = object : ReflectiveEqualsHashCode(), Size1Wrapper {
    override val wrapped: Size1
        get() = this@copy

    @Composable
    override fun contentArrangement(): Arrangement.Vertical = contentArrangement.invoke(this)

    @Composable
    override fun contentAlignment(): Alignment.Horizontal = contentAlignment.invoke(this)
}

@Composable
inline fun Size1.rememberCopy(
    crossinline contentArrangement: @Composable Size1Wrapper.() -> Arrangement.Vertical = { wrapped.contentArrangement() },
    crossinline contentAlignment: @Composable Size1Wrapper.() -> Alignment.Horizontal = { wrapped.contentAlignment() },
): Size1 = remember {
    copy(contentArrangement = contentArrangement, contentAlignment = contentAlignment)
}
