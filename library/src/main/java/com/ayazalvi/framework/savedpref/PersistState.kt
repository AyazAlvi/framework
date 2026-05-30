package com.ayazalvi.framework.savedpref

import android.content.Context
import android.view.View
import com.ayazalvi.framework.core.screen.Screen

abstract class PersistState<T> (private val screen: Screen<*>, protected val key: String, protected val defaultValue: T) {

    protected abstract operator fun invoke () : T
    protected abstract operator fun invoke (value: T)

    protected val sp = screen.activity.applicationContext.getSharedPreferences(javaClass.simpleName, Context.MODE_PRIVATE)!!

    init { if (!screen.stateDataRegistry.containsKey(key)) { screen.update(key, invoke()) } }

    var value: T
        get() = invoke()
        set(newValue) { invoke(newValue); screen.update(key, newValue) }

    fun <V : View> bind(view: V, bindingBlock: (value: T, view: V) -> Unit) {
        screen.bind(key, view, bindingBlock)
    }

}
