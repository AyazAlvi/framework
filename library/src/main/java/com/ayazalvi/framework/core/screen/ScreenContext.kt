package com.ayazalvi.framework.core.screen

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import com.ayazalvi.framework.core.navigator.Navigator


// --- 2. THE CONTEXT WRAPPER ---
class ScreenContext(
    val state: SavedStateHandle,
    val navigator: Navigator,
    val arguments: Bundle?
)
