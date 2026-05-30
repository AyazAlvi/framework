package com.ayazalvi.framework.core.internal

import android.content.Context
import android.util.TypedValue


interface BackPressHandler {
    fun updateBackPressState(isOverridden: Boolean)
}

// --- Helper for Dynamic Theme Animations ---
fun Context.getThemeAnimation(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.resourceId
}
