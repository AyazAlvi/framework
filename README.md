# Framework

[![](https://jitpack.io/v/AyazAlvi/framework.svg)](https://jitpack.io/#AyazAlvi/framework)

A lightweight Android screen management library. Stop writing Fragment boilerplate, ViewModel wiring, and back-stack plumbing — and start writing business logic.

Framework gives you a single `Screen` abstraction that handles navigation, state, lifecycle, and result passing out of the box. Built on the standard Fragment back stack, no black-box navigation graphs.

---

## What it fixes

- **No more Fragment transactions** — navigation is a one-liner with full type safety
- **No more ViewModel factories** — every screen gets a scoped, rotation-safe coordinator automatically
- **No more `onSaveInstanceState` boilerplate** — state lives in `SavedStateHandle` and binds to views declaratively
- **No more dialog/sheet plumbing** — dialogs and bottom sheets are just screens, presented through the same navigator
- **No more result callbacks wired by hand** — result propagation between any two screens is built in
- **No more back press juggling** — override back per-screen without touching the Activity

---

## Setup

### Groovy (`build.gradle`)

```groovy
// settings.gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}

// app/build.gradle
dependencies {
    implementation 'com.github.AyazAlvi:framework:<version>'
}
```

### Kotlin DSL (`build.gradle.kts`)

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        maven("https://jitpack.io")
    }
}

// app/build.gradle.kts
dependencies {
    implementation("com.github.AyazAlvi:framework:<version>")
}
```

Replace `<version>` with the latest badge version above.

---

## Get Started

### 1. Set up your Activity

Extend `FrameworkActivity`, declare your container, and register every screen your app uses:

```kotlin
class MainActivity : FrameworkActivity() {

    override val fragmentContainerId = R.id.container

    override fun onRegisterScreens(registry: ScreenRegistry) {
        registry.register(FragmentHomeBinding::inflate, ::HomeScreen)
        registry.register(FragmentDetailBinding::inflate, ::DetailScreen)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null) push<HomeScreen>()
    }
}
```

### 2. Create a Screen

Extend `Screen<VB>` with your `ViewBinding`. Use `onFirstLaunch()` for one-time setup and `onUI()` to wire your views — both run at the right time automatically:

```kotlin
class HomeScreen(context: ScreenContext) : Screen<FragmentHomeBinding>(context) {

    private val count by lazy { state("count", 0) }

    override fun onFirstLaunch() {
        // Runs once — survives rotation
    }

    override fun onUI() {
        // Runs every time the view is created

        count.bind(ui.tvCount) { value, view ->
            view.text = "Count: $value"
        }

        ui.btnIncrement.setOnClickListener {
            count.value++ // updates bound views automatically
        }

        ui.btnNext.setOnClickListener {
            navigator.push<DetailScreen>(args = bundleOf("id" to 42))
        }
    }
}
```

That's it. No `ViewModelProvider`, no `FragmentTransaction`, no `onViewCreated` lifecycle dance.

---

## Navigation

```kotlin
navigator.push<DetailScreen>()
navigator.push<DetailScreen>(args = bundleOf("userId" to "abc123"))
navigator.replace<HomeScreen>()
navigator.pop()
navigator.popToRoot()
navigator.push<PanelScreen>(containerId = R.id.panel_container) // any container
```

---

## Dialogs & Bottom Sheets

Register them like any other screen, present them through the navigator:

```kotlin
navigator.presentDialog<ConfirmDialog>()
navigator.presentBottomSheet<OptionsSheet>(args = bundleOf("title" to "Pick one"))
navigator.dismissCurrentDialog()
navigator.dismissCurrentBottomSheet()
```

Call `close()` inside any screen — it detects the host type and does the right thing:

```kotlin
ui.btnCancel.setOnClickListener { close() }
```

---

## Result Propagation

```kotlin
// Parent — push and listen
pushForResult<DetailScreen>(requestKey = "detail_result") { bundle ->
    val name = bundle.getString("name")
}

// Child — send result and close
popWithResult("detail_result", bundleOf("name" to "Ayaz"))
```

Works identically with `presentDialogForResult` and `presentBottomSheetForResult`.

---

## Custom Back Press

```kotlin
override fun onFirstLaunch() { backPressOverrideEnabled = true }

override fun onBackPressed() {
    if (hasUnsavedChanges()) showDiscardDialog()
    else navigator.performDefaultBack()
}
```

---

## Shared ViewModels

```kotlin
private val vm: SharedAppViewModel by lazy { sharedViewModel() }
// Same instance across all screens in the Activity
```

---

## License

```
Copyright 2026 Ayaz Alvi

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0
```
