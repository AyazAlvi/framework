package com.ayazalvi.framework.core.screen

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import kotlin.reflect.KClass

// --- 3. ACTIVITY-SCOPED REGISTRY (INLINE / REIFIED) ---
class ScreenRegistry {
    @PublishedApi
    internal val definitions = HashMap<KClass<out Screen<*>>, ScreenDefinition>()

    inline fun <reified S : Screen<*>> register(
        noinline inflateBlock: (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding,
        noinline factory: (ScreenContext) -> S
    ) {
        definitions[S::class] = ScreenDefinition(
            inflate = { inflater, container -> inflateBlock(inflater, container, false) },
            factory = { context -> factory(context) }
        )
    }
}