package com.ayazalvi.framework.utils.permission

import android.Manifest
import android.content.pm.PackageManager.PERMISSION_GRANTED
import androidx.core.content.ContextCompat
import com.ayazalvi.framework.core.screen.Screen

typealias Perm = Manifest.permission
typealias PermGroup = Manifest.permission_group

fun Screen<*>.permission (perm: String) : Boolean {
    return ContextCompat.checkSelfPermission(activity, perm) == PERMISSION_GRANTED
}
