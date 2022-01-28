package com.toasttab.pulseman.state

import androidx.compose.runtime.MutableState

fun <T> MutableState<T>.onStateChange(newState: T) {
    this.value = newState
}
