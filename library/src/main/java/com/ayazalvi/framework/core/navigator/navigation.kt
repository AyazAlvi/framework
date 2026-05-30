package com.ayazalvi.framework.core.navigator

import android.os.Bundle
import android.view.View
import com.ayazalvi.framework.core.screen.Screen

inline fun <reified S : Screen<*>> Navigator.push(args: Bundle? = null, sharedElement: View? = null, containerId: Int? = null) = push(S::class, args, sharedElement, containerId)
inline fun <reified S : Screen<*>> Navigator.replace(args: Bundle? = null, containerId: Int? = null) = replace(S::class, args, containerId)
inline fun <reified S : Screen<*>> Navigator.presentDialog(args: Bundle? = null) = presentDialog(S::class, args)
inline fun <reified S : Screen<*>> Navigator.presentBottomSheet(args: Bundle? = null) = presentBottomSheet(S::class, args)
