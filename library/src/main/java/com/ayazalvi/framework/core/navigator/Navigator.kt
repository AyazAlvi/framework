package com.ayazalvi.framework.core.navigator

import android.os.Bundle
import android.view.View
import com.ayazalvi.framework.core.screen.Screen
import kotlin.reflect.KClass


// --- 1. TYPE-SAFE NAVIGATION INTERFACE ---
interface Navigator {
    // Standard Navigation (Now with dynamic container support)
    fun <S : Screen<*>> push(screenClass: KClass<S>, args: Bundle? = null, sharedElement: View? = null, containerId: Int? = null)
    fun <S : Screen<*>> replace(screenClass: KClass<S>, args: Bundle? = null, containerId: Int? = null)
    fun pop()
    fun popToRoot()
    fun performDefaultBack() // Escape hatch for custom overridden back actions

    // Multidimensional Presentation (Dialogs & Bottom Sheets)
    fun <S : Screen<*>> presentDialog(screenClass: KClass<S>, args: Bundle? = null)
    fun <S : Screen<*>> presentBottomSheet(screenClass: KClass<S>, args: Bundle? = null)
    fun dismissCurrentDialog()
    fun dismissCurrentBottomSheet()
}
