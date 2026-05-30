package com.ayazalvi.framework.core.activity

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.ayazalvi.framework.core.fragments.ScreenHostBottomSheetFragment
import com.ayazalvi.framework.core.fragments.ScreenHostDialogFragment
import com.ayazalvi.framework.core.fragments.ScreenHostFragment
import com.ayazalvi.framework.core.internal.getThemeAnimation
import com.ayazalvi.framework.core.navigator.Navigator
import com.ayazalvi.framework.core.screen.Screen
import com.ayazalvi.framework.core.screen.ScreenRegistry
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.reflect.KClass

// --- 7. THE MODERN FRAMEWORK ACTIVITY ---
abstract class FrameworkActivity : AppCompatActivity(), Navigator {

    abstract val fragmentContainerId: Int
    internal val registry = ScreenRegistry()

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Find current active fragment. Prioritizes the main container.
            // Dialogs automatically handle their own back-press native to Android Window.
            val currentFragment = supportFragmentManager.findFragmentById(fragmentContainerId) as? ScreenHostFragment
            val currentScreen = currentFragment?.screen

            // Route through custom interceptor logic rules if configured
            if (currentScreen != null && currentScreen.backPressOverrideEnabled) {
                currentScreen.onBackPressed()
            } else {
                executeDefaultBack()
            }
        }
    }

    internal fun executeDefaultBack() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            // Escape loop cleanly on first/root screens to finish parent activity execution context
            backCallback.isEnabled = false
            onBackPressedDispatcher.onBackPressed()
            backCallback.isEnabled = true
        }
    }

    override fun performDefaultBack() {
        executeDefaultBack()
    }

    abstract fun onRegisterScreens(registry: ScreenRegistry)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onRegisterScreens(registry)
        onBackPressedDispatcher.addCallback(this, backCallback)
    }

    override fun <S : Screen<*>> push(screenClass: KClass<S>, args: Bundle?, sharedElement: View?, containerId: Int?) {
        val targetContainerId = containerId ?: fragmentContainerId
        val fragment = ScreenHostFragment.create(screenClass, args)
        val transaction = supportFragmentManager.beginTransaction()

        if (sharedElement != null) { transaction.addSharedElement(sharedElement, sharedElement.transitionName) }

        transaction.setReorderingAllowed(true)

        // Dynamically resolve standard animations from the app theme
        val enterAnim = getThemeAnimation(android.R.attr.activityOpenEnterAnimation)
        val exitAnim = getThemeAnimation(android.R.attr.activityOpenExitAnimation)
        val popEnterAnim = getThemeAnimation(android.R.attr.activityCloseEnterAnimation)
        val popExitAnim = getThemeAnimation(android.R.attr.activityCloseExitAnimation)

        if (enterAnim != 0 && exitAnim != 0) {
            transaction.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
        }

        transaction.replace(targetContainerId, fragment)
        if (supportFragmentManager.fragments.isNotEmpty()) { transaction.addToBackStack(screenClass.java.name) }

        transaction.commit()
    }

    override fun <S : Screen<*>> replace(screenClass: KClass<S>, args: Bundle?, containerId: Int?) {
        val targetContainerId = containerId ?: fragmentContainerId
        val fragment = ScreenHostFragment.create(screenClass, args)
        supportFragmentManager.beginTransaction()
            .replace(targetContainerId, fragment)
            .commit()
    }

    override fun pop() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            finish()
        }
    }

    override fun popToRoot() {
        if (supportFragmentManager.backStackEntryCount > 0) {
            val first = supportFragmentManager.getBackStackEntryAt(0)
            supportFragmentManager.popBackStack(first.id, 0)
        }
    }

    override fun <S : Screen<*>> presentDialog(screenClass: KClass<S>, args: Bundle?) {
        val dialog = ScreenHostDialogFragment.create(screenClass, args)
        dialog.show(supportFragmentManager, screenClass.java.name)
    }

    override fun <S : Screen<*>> presentBottomSheet(screenClass: KClass<S>, args: Bundle?) {
        val bottomSheet = ScreenHostBottomSheetFragment.create(screenClass, args)
        bottomSheet.show(supportFragmentManager, screenClass.java.name)
    }

    override fun dismissCurrentDialog() {
        // Exclude bottom sheets to prevent dismissing the wrong popup
        supportFragmentManager.fragments
            .filterIsInstance<DialogFragment>().lastOrNull { it !is BottomSheetDialogFragment }?.dismiss()
    }

    override fun dismissCurrentBottomSheet() {
        supportFragmentManager.fragments
            .filterIsInstance<BottomSheetDialogFragment>()
            .lastOrNull()?.dismiss()
    }
}