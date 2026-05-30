package com.ayazalvi.framework.savedpref

import com.ayazalvi.framework.core.screen.Screen
import java.io.File


class ObjectPersist<T> (screen: Screen<*>, key: String, defaultValue: T) : PersistState<T>(screen, key, defaultValue) {
    private val internalDirectory = File(screen.activity.filesDir, javaClass.simpleName)

    override fun invoke() = LocalDB.read(internalDirectory, key)?:defaultValue!!
    override fun invoke(value: T) { if(value == null) LocalDB.delete(internalDirectory, key) else LocalDB.write(internalDirectory, key, value) }

}
