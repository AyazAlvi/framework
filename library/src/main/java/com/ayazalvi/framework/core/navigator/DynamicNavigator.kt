package com.ayazalvi.framework.core.navigator

import android.os.Bundle
import android.view.View
import com.ayazalvi.framework.core.screen.Screen
import kotlin.reflect.KClass


internal class DynamicNavigator : Navigator {
    var current: Navigator? = null
    override fun <S : Screen<*>> push(screenClass: KClass<S>, args: Bundle?, sharedElement: View?, containerId: Int?) { current?.push(screenClass, args, sharedElement, containerId) }
    override fun <S : Screen<*>> replace(screenClass: KClass<S>, args: Bundle?, containerId: Int?) { current?.replace(screenClass, args, containerId) }
    override fun pop() { current?.pop() }
    override fun popToRoot() { current?.popToRoot() }
    override fun performDefaultBack() { current?.performDefaultBack() }
    override fun <S : Screen<*>> presentDialog(screenClass: KClass<S>, args: Bundle?) { current?.presentDialog(screenClass, args) }
    override fun <S : Screen<*>> presentBottomSheet(screenClass: KClass<S>, args: Bundle?) { current?.presentBottomSheet(screenClass, args) }
    override fun dismissCurrentDialog() { current?.dismissCurrentDialog() }
    override fun dismissCurrentBottomSheet() { current?.dismissCurrentBottomSheet() }
}
