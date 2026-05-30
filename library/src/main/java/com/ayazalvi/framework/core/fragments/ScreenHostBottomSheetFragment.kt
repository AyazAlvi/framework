package com.ayazalvi.framework.core.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.lifecycle.ViewModelProvider
import com.ayazalvi.framework.core.activity.FrameworkActivity
import com.ayazalvi.framework.core.internal.BackPressHandler
import com.ayazalvi.framework.core.screen.Screen
import com.ayazalvi.framework.core.screen.ScreenCoordinator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.reflect.KClass


// BOTTOM SHEET HOST
class ScreenHostBottomSheetFragment : BottomSheetDialogFragment(), BackPressHandler {

    private lateinit var screenClass: KClass<out Screen<*>>
    internal lateinit var screen: Screen<*>
    private lateinit var frameworkActivity: FrameworkActivity

    companion object {
        private const val ARG_CLASS_NAME = "SCREEN_CLASS_NAME"
        private const val ARG_CUSTOM_DATA = "CUSTOM_DATA"

        fun create(screenClass: KClass<out Screen<*>>, args: Bundle? = null): ScreenHostBottomSheetFragment {
            return ScreenHostBottomSheetFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CLASS_NAME, screenClass.java.name)
                    if (args != null) putBundle(ARG_CUSTOM_DATA, args)
                }
            }
        }
    }

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            screen.onBackPressed()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        frameworkActivity = requireActivity() as FrameworkActivity

        val className = arguments?.getString(ARG_CLASS_NAME) ?: throw IllegalStateException("Missing Screen Class")
        @Suppress("UNCHECKED_CAST")
        screenClass = Class.forName(className).kotlin as KClass<out Screen<*>>

        val customArgs = arguments?.getBundle(ARG_CUSTOM_DATA)
        val coordinator = ViewModelProvider(this)[ScreenCoordinator::class.java]

        coordinator.dynamicNavigator.current = frameworkActivity
        screen = coordinator.getOrCreateScreen(screenClass, frameworkActivity.registry, customArgs)

        screen.hostFragment = this
        screen.performFirstLaunch()

    }

    override fun onStart() {
        super.onStart()
        val dispatcher = (dialog as? ComponentDialog)?.onBackPressedDispatcher
            ?: requireActivity().onBackPressedDispatcher
        dispatcher.addCallback(viewLifecycleOwner, backCallback)
    }

    override fun updateBackPressState(isOverridden: Boolean) {
        backCallback.isEnabled = isOverridden
        isCancelable = !isOverridden
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val definition = frameworkActivity.registry.definitions[screenClass]!!
        val binding = definition.inflate(inflater, container)
        screen.attachUI(binding)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        screen.detachUI()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::screen.isInitialized) screen.hostFragment = null
        ViewModelProvider(this)[ScreenCoordinator::class.java].dynamicNavigator.current = null
    }
}
