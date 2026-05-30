package com.ayazalvi.framework.core.screen

import android.os.Bundle
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.ayazalvi.framework.core.navigator.DynamicNavigator
import kotlin.reflect.KClass


// --- 5. THE SCREEN COORDINATOR ---
class ScreenCoordinator(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private var retainedScreen: Screen<*>? = null
    internal val dynamicNavigator = DynamicNavigator()

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Screen<*>> getOrCreateScreen(
        screenClass: KClass<out Screen<*>>,
        registry: ScreenRegistry,
        args: Bundle?
    ): T {
        if (retainedScreen == null) {
            val definition = registry.definitions[screenClass]
                ?: throw IllegalStateException("Screen ${screenClass.simpleName} is not registered in this Activity.")

            val context = ScreenContext(savedStateHandle, dynamicNavigator, args)
            retainedScreen = definition.factory(context)
        }
        return retainedScreen as T
    }

    override fun onCleared() {
        super.onCleared()
        retainedScreen?.onCleared()
        retainedScreen = null
        dynamicNavigator.current = null
    }
}
