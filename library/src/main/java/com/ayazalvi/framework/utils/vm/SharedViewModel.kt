package com.ayazalvi.framework.utils.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ayazalvi.framework.core.screen.Screen

/**
 * Scopes a ViewModel to the underlying FrameworkActivity.
 * Any screen calling this with the same ViewModel class will receive the exact same shared instance.
 */
inline fun <reified VM : ViewModel> Screen<*>.sharedViewModel(): VM {
    return ViewModelProvider(this.activity)[VM::class.java]
}

inline fun <reified VM : ViewModel> Screen<*>.sharedVM () : Lazy<VM> = lazy { sharedViewModel() }
