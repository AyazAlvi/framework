package com.ayazalvi.framework.core.screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding

data class ScreenDefinition(
    val inflate: (LayoutInflater, ViewGroup?) -> ViewBinding,
    val factory: (ScreenContext) -> Screen<*>
)