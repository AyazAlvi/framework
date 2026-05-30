package com.ayazalvi.framework.savedpref

import androidx.core.content.edit
import com.ayazalvi.framework.core.screen.Screen
import java.io.Serializable

fun <T: Serializable> Screen<*>.persist (key: String, defaultValue: T) = lazy { ObjectPersist(this, key, defaultValue) }

fun Screen<*>.persist (key: String, defaultValue: String) = lazy {
    object : PersistState<String>(this, key, defaultValue) {
        override fun invoke() = sp.getString(key, defaultValue)!!
        override fun invoke(value: String) { sp.edit { putString(key, value) } }
    }
}
fun Screen<*>.persist (key: String, defaultValue: Int) = lazy {
    object : PersistState<Int>(this, key, defaultValue) {
        override fun invoke() = sp.getInt(key, defaultValue)
        override fun invoke(value: Int) { sp.edit { putInt(key, value) } }
    }
}
fun Screen<*>.persist (key: String, defaultValue: Float) = lazy {
    object : PersistState<Float>(this, key, defaultValue) {
        override fun invoke() = sp.getFloat(key, defaultValue)
        override fun invoke(value: Float) { sp.edit { putFloat(key, value) } }
    }
}
fun Screen<*>.persist (key: String, defaultValue: Boolean) = lazy {
    object : PersistState<Boolean>(this, key, defaultValue) {
        override fun invoke() = sp.getBoolean(key, defaultValue)
        override fun invoke(value: Boolean) { sp.edit { putBoolean(key, value) } }
    }
}

inline fun <reified T: Enum<T>> Screen<*>.persist (key: String, defaultValue: T) = lazy {
    object : PersistState<T>(this, key, defaultValue) {
        override fun invoke() = enumValueOf<T>(sp.getString(key, defaultValue.name)!!)
        override fun invoke(value: T) { sp.edit { putString(key, value.name) } }
    }
}
