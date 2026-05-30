package com.ayazalvi.framework.core.screen

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.viewbinding.ViewBinding
import com.ayazalvi.framework.core.activity.FrameworkActivity
import com.ayazalvi.framework.core.internal.BackPressHandler
import com.ayazalvi.framework.core.navigator.Navigator
import com.ayazalvi.framework.utils.exception.ActionableException
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


abstract class Screen<VB : ViewBinding>(
    private val context: ScreenContext
) {
    // Internal platform lifecycle bridge
    internal var hostFragment: Fragment? = null

    // Safe scoped access to host elements
    protected val fragment: Fragment
        get() = hostFragment ?: throw IllegalStateException("Fragment is not attached right now.")

    val activity: FrameworkActivity
        get() = (hostFragment?.requireActivity() as? FrameworkActivity)
            ?: throw IllegalStateException("Activity is not attached right now.")

    // Back Navigation Override Configurations
    var backPressOverrideEnabled: Boolean = false
        set(value) {
            field = value
            // Notify the host if it's a BackPressHandler (which all 3 hosts will be)
            (hostFragment as? BackPressHandler)?.updateBackPressState(value)
        }

    open fun onBackPressed() { close() }

    // Expose context parameters cleanly
    val navigator: Navigator get() = context.navigator
    protected val savedStateHandle: SavedStateHandle get() = context.state
    protected val arguments: Bundle? get() = context.arguments

    val stateDataRegistry = HashMap<String, Any?>()
    internal val bindingRegistry = HashMap<String, MutableList<(Any?) -> Unit>>()

    private var isFirstLaunchExecuted = false
    private var _ui: VB? = null
    val ui: VB get() = _ui ?: throw IllegalStateException("UI is not attached right now.")

    open fun onFirstLaunch() {}
    fun onUIInternal() {
        if (ui.root.background == null) ui.root.background = activity.window.decorView.background
    }
    abstract fun onUI()

    internal fun performFirstLaunch() {
        if (!isFirstLaunchExecuted) {
            savedStateHandle.keys().forEach { key ->
                stateDataRegistry[key] = savedStateHandle.get<Any?>(key)
            }
            onFirstLaunch()
            isFirstLaunchExecuted = true
        }
    }

    internal fun attachUI(binding: ViewBinding) {
        @Suppress("UNCHECKED_CAST")
        _ui = binding as VB
        bindingRegistry.clear()
        onUIInternal()
        try { onUI() } catch (e: Exception) { onErrorReceived(e) }
        stateDataRegistry.forEach { (key, data) -> executeBindings(key, data) }
    }

    open fun onActionException (action: ActionableException) { onErrorReceived(Throwable(action.msg)) }
    open fun onErrorReceived (e: Throwable) { Log.e("Class::" + this::class.simpleName, "Class::" + this::class.simpleName + " --- $e") }

    internal fun detachUI() {
        _ui = null
        bindingRegistry.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun <D : Any, V : View> bind(key: String, view: V, bindingBlock: (data: D, viewToUpdate: V) -> Unit) {
        val list = bindingRegistry.getOrPut(key) { mutableListOf() }
        val genericBlock: (Any?) -> Unit = { freshData ->
            if (freshData != null) bindingBlock(freshData as D, view)
        }
        list.add(genericBlock)
        if (stateDataRegistry.containsKey(key)) genericBlock(stateDataRegistry[key])
    }

    fun update(key: String, data: Any?) {
        stateDataRegistry[key] = data
        savedStateHandle[key] = data
        executeBindings(key, data)
    }

    private fun executeBindings(key: String, data: Any?) {
        if (_ui == null) return
        if (Looper.myLooper() == Looper.getMainLooper()) {
            bindingRegistry[key]?.forEach { it(data) }
        } else {
            Handler(Looper.getMainLooper()).post {
                if (_ui != null) bindingRegistry[key]?.forEach { it(data) }
            }
        }
    }

    internal fun onCleared() {
        stateDataRegistry.clear()
        bindingRegistry.clear()
        _ui = null
    }



    // --- Result Propagation & Smart Closing API ---

    /**
     * Intelligently closes the current screen by detecting its host type.
     * Prevents back-stack bugs when closing dialogs or bottom sheets.
     */
    fun close() {
        when (hostFragment) {
            is BottomSheetDialogFragment -> navigator.dismissCurrentBottomSheet()
            is DialogFragment -> navigator.dismissCurrentDialog() // Will catch ScreenHostDialogFragment
            else -> navigator.pop() // Standard ScreenHostFragment
        }
    }

    // --- Result Propagation API ---

    fun setResult(requestKey: String, result: Bundle) {
        hostFragment?.parentFragmentManager?.setFragmentResult(requestKey, result)
//        activity.supportFragmentManager.setFragmentResult(requestKey, result)
    }

    fun listenForResult(requestKey: String, onResult: (Bundle) -> Unit) {
        val f = hostFragment ?: return
        f.parentFragmentManager.setFragmentResultListener(requestKey, f) { _, bundle ->
            onResult(bundle)
        }
    }

    fun popWithResult(requestKey: String, result: Bundle) {
        setResult(requestKey, result)
        close() // Replaced 'navigator.pop()' with smart closing
    }


}
