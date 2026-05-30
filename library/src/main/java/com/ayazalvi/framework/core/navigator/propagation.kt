package com.ayazalvi.framework.core.navigator

import android.os.Bundle
import android.view.View
import com.ayazalvi.framework.core.screen.Screen

// Inline extension for beautiful result propagation
inline fun <reified S : Screen<*>> Screen<*>.pushForResult(
    requestKey: String,
    args: Bundle? = null,
    sharedElement: View? = null,
    containerId: Int? = null,
    noinline onResult: (Bundle) -> Unit
) {
    listenForResult(requestKey, onResult)
//    navigator.push(S::class, args, sharedElement, containerId)
    navigator.push<S>(args, sharedElement, containerId)
}
// Inline extensions for multidimensional result propagation
inline fun <reified S : Screen<*>> Screen<*>.presentDialogForResult(
    requestKey: String,
    args: Bundle? = null,
    noinline onResult: (Bundle) -> Unit
) {
    listenForResult(requestKey, onResult)
//    navigator.presentDialog(S::class, args)
    navigator.presentDialog<S>(args)
}

inline fun <reified S : Screen<*>> Screen<*>.presentBottomSheetForResult(
    requestKey: String,
    args: Bundle? = null,
    noinline onResult: (Bundle) -> Unit
) {
    listenForResult(requestKey, onResult)
//    navigator.presentBottomSheet(S::class, args)
    navigator.presentBottomSheet<S>(args)
}


