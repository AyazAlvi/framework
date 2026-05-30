package com.ayazalvi.framework.utils.permission

import androidx.activity.result.contract.ActivityResultContracts
import com.ayazalvi.framework.core.screen.Screen

class PermissionHelper (screen: Screen<*>) {

    private val permLauncher by lazy {
        screen.hostFragment!!.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { perms ->
            onResult?.invoke(perms)
        }
    }
    var onResult: ((perms: Map<String, Boolean>) -> Unit)? = null

    fun requestPermission (permissions: Array<String>) { permLauncher.launch(permissions) }

}