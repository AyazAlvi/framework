package com.ayazalvi.framework.core.screen

import android.view.View


// --- 4. THE BASE SCREEN & STATE WRAPPER ---
class ScreenState<T>(
    private val screen: Screen<*>,
    private val key: String,
    initialValue: T
) {
    init {
        if (!screen.stateDataRegistry.containsKey(key)) {
            screen.update(key, initialValue)
        }
    }

    var value: T
        @Suppress("UNCHECKED_CAST")
        get() = screen.stateDataRegistry[key] as T
        set(newValue) { screen.update(key, newValue) }

    fun <V : View> bind(view: V, bindingBlock: (value: T, view: V) -> Unit) {
        screen.bind(key, view, bindingBlock)
    }
}

fun <T> Screen<*>.state(key: String, initialValue: T) = ScreenState(this, key, initialValue)
