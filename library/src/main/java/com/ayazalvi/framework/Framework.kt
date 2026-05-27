package com.ayazalvi.framework


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.viewbinding.ViewBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.Serializable
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
fun bundleOf(vararg pairs: Pair<String, Any?>): Bundle {
    val bundle = Bundle()
    for ((key, value) in pairs) {
        if (value == null) {
            bundle.remove(key)
            continue
        }
        when (value) {
            is String -> bundle.putString(key, value)
            is Int -> bundle.putInt(key, value)
            is Long -> bundle.putLong(key, value)
            is Float -> bundle.putFloat(key, value)
            is Double -> bundle.putDouble(key, value)
            is Boolean -> bundle.putBoolean(key, value)
            is Short -> bundle.putShort(key, value)
            is Byte -> bundle.putByte(key, value)
            is Char -> bundle.putChar(key, value)
            is CharSequence -> bundle.putCharSequence(key, value)
            is Parcelable -> bundle.putParcelable(key, value)
            // Array/ArrayList support
            is IntArray -> bundle.putIntArray(key, value)
            is LongArray -> bundle.putLongArray(key, value)
            is FloatArray -> bundle.putFloatArray(key, value)
            is BooleanArray -> bundle.putBooleanArray(key, value)
            is ByteArray -> bundle.putByteArray(key, value)
            is ShortArray -> bundle.putShortArray(key, value)
            is CharArray -> bundle.putCharArray(key, value)
            is Array<*> -> when {
                value.isArrayOf<String>() -> bundle.putStringArray(key, value as Array<String>)
                value.isArrayOf<Parcelable>() -> bundle.putParcelableArray(key, value as Array<Parcelable>)
                else -> throw IllegalArgumentException("Unsupported Array type for key: $key")
            }
            is Serializable -> bundle.putSerializable(key, value)
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java.simpleName} for key: $key")
        }
    }
    return bundle
}

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

inline fun <reified S : Screen<*>> Navigator.push(args: Bundle? = null, sharedElement: View? = null, containerId: Int? = null) = push(S::class, args, sharedElement, containerId)
inline fun <reified S : Screen<*>> Navigator.replace(args: Bundle? = null, containerId: Int? = null) = replace(S::class, args, containerId)
inline fun <reified S : Screen<*>> Navigator.presentDialog(args: Bundle? = null) = presentDialog(S::class, args)
inline fun <reified S : Screen<*>> Navigator.presentBottomSheet(args: Bundle? = null) = presentBottomSheet(S::class, args)

// Inline extension for beautiful result propagation
inline fun <reified S : Screen<*>> Screen<*>.pushForResult(
    requestKey: String,
    args: Bundle? = null,
    sharedElement: View? = null,
    containerId: Int? = null,
    noinline onResult: (Bundle) -> Unit
) {
    listenForResult(requestKey, onResult)
    navigator.push(S::class, args, sharedElement, containerId)
}
// Inline extensions for multidimensional result propagation
inline fun <reified S : Screen<*>> Screen<*>.presentDialogForResult(
    requestKey: String,
    args: Bundle? = null,
    noinline onResult: (Bundle) -> Unit
) {
    listenForResult(requestKey, onResult)
    navigator.presentDialog(S::class, args)
}

inline fun <reified S : Screen<*>> Screen<*>.presentBottomSheetForResult(
    requestKey: String,
    args: Bundle? = null,
    noinline onResult: (Bundle) -> Unit
) {
    listenForResult(requestKey, onResult)
    navigator.presentBottomSheet(S::class, args)
}

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

// --- 2. THE CONTEXT WRAPPER ---
class ScreenContext(
    val state: SavedStateHandle,
    val navigator: Navigator,
    val arguments: Bundle?
)

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

data class ScreenDefinition(
    val inflate: (LayoutInflater, ViewGroup?) -> ViewBinding,
    val factory: (ScreenContext) -> Screen<*>
)

// --- 4. THE BASE SCREEN & STATE WRAPPER ---
class ScreenState<T>(
    private val screen: Screen<*>,
    private val key: String,
    initialValue: T
) {
    init {
        if (!screen.stateDataRegistry.containsKey(key)) {
            screen.update(key, initialValue)
        }
    }

    var value: T
        @Suppress("UNCHECKED_CAST")
        get() = screen.stateDataRegistry[key] as T
        set(newValue) { screen.update(key, newValue) }

    fun <V : View> bind(view: V, bindingBlock: (value: T, view: V) -> Unit) {
        screen.bind(key, view, bindingBlock)
    }
}

fun <T> Screen<*>.state(key: String, initialValue: T) = ScreenState(this, key, initialValue)

/**
 * Scopes a ViewModel to the underlying FrameworkActivity.
 * Any screen calling this with the same ViewModel class will receive the exact same shared instance.
 */
inline fun <reified VM : ViewModel> Screen<*>.sharedViewModel(): VM {
    return ViewModelProvider(this.activity)[VM::class.java]
}

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

    open fun onBackPressed() { navigator.performDefaultBack() }

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
        onUI()
        stateDataRegistry.forEach { (key, data) -> executeBindings(key, data) }
    }

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
//        hostFragment?.parentFragmentManager?.setFragmentResult(requestKey, result)
        activity.supportFragmentManager.setFragmentResult(requestKey, result)
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

// --- 5. THE SCREEN COORDINATOR ---
class ScreenCoordinator(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private var retainedScreen: Screen<*>? = null
    internal val dynamicNavigator = DynamicNavigator()

    @Suppress("UNCHECKED_CAST")
    internal fun <T : Screen<*>> getOrCreateScreen(
        screenClass: KClass<out Screen<*>>,
        registry: ScreenRegistry,
        args: Bundle?
    ): T {
        if (retainedScreen == null) {
            val definition = registry.definitions[screenClass]
                ?: throw IllegalStateException("Screen ${screenClass.simpleName} is not registered in this Activity.")

            val context = ScreenContext(savedStateHandle, dynamicNavigator, args)
            retainedScreen = definition.factory(context)
        }
        return retainedScreen as T
    }

    override fun onCleared() {
        super.onCleared()
        retainedScreen?.onCleared()
        retainedScreen = null
        dynamicNavigator.current = null
    }
}

// --- 6. HOST FRAGMENTS (STANDARD, DIALOG, & BOTTOM SHEET) ---

// STANDARD HOST
class ScreenHostFragment : Fragment(), BackPressHandler {
    private lateinit var screenClass: KClass<out Screen<*>>
    internal lateinit var screen: Screen<*>
    private lateinit var frameworkActivity: FrameworkActivity

    companion object {
        private const val ARG_CLASS_NAME = "SCREEN_CLASS_NAME"
        private const val ARG_CUSTOM_DATA = "CUSTOM_DATA"

        fun create(screenClass: KClass<out Screen<*>>, args: Bundle? = null): ScreenHostFragment {
            return ScreenHostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CLASS_NAME, screenClass.java.name)
                    if (args != null) putBundle(ARG_CUSTOM_DATA, args)
                }
            }
        }
    }

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() { screen.onBackPressed() }
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

        requireActivity().onBackPressedDispatcher.addCallback(this, backCallback)

    }

    override fun updateBackPressState(isOverridden: Boolean) { backCallback.isEnabled = isOverridden }

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

interface BackPressHandler {
    fun updateBackPressState(isOverridden: Boolean)
}

// DIALOG HOST
class ScreenHostDialogFragment : DialogFragment(), BackPressHandler {

    private lateinit var screenClass: KClass<out Screen<*>>
    internal lateinit var screen: Screen<*>
    private lateinit var frameworkActivity: FrameworkActivity

    companion object {
        private const val ARG_CLASS_NAME = "SCREEN_CLASS_NAME"
        private const val ARG_CUSTOM_DATA = "CUSTOM_DATA"

        fun create(screenClass: KClass<out Screen<*>>, args: Bundle? = null): ScreenHostDialogFragment {
            return ScreenHostDialogFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CLASS_NAME, screenClass.java.name)
                    if (args != null) putBundle(ARG_CUSTOM_DATA, args)
                }
            }
        }
    }

    private val backCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() { screen.onBackPressed() }
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
        val dispatcher = (dialog as? androidx.activity.ComponentDialog)?.onBackPressedDispatcher
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
        val dispatcher = (dialog as? androidx.activity.ComponentDialog)?.onBackPressedDispatcher
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

// --- Helper for Dynamic Theme Animations ---
fun Context.getThemeAnimation(attr: Int): Int {
    val typedValue = TypedValue()
    theme.resolveAttribute(attr, typedValue, true)
    return typedValue.resourceId
}

// --- 7. THE MODERN FRAMEWORK ACTIVITY ---
abstract class FrameworkActivity : AppCompatActivity(), Navigator {

    abstract val fragmentContainerId: Int
    internal val registry = ScreenRegistry()

    private val backCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            // Find current active fragment. Prioritizes the main container.
            // Dialogs automatically handle their own backpress native to Android Window.
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
            .filterIsInstance<DialogFragment>()
            .filter { it !is BottomSheetDialogFragment }
            .lastOrNull()?.dismiss()
    }

    override fun dismissCurrentBottomSheet() {
        supportFragmentManager.fragments
            .filterIsInstance<BottomSheetDialogFragment>()
            .lastOrNull()?.dismiss()
    }
}
