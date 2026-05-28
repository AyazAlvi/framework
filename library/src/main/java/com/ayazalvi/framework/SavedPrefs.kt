package com.ayazalvi.framework

import android.content.Context
import android.view.View
import androidx.core.content.edit
import kotlinx.serialization.json.Json
import java.io.File
import java.io.Serializable

object LocalDB {

    inline fun <reified T: Any> read (baseFile: File, path: String) : T? {
        val file = File(baseFile, path)
        if (!file.isFile) return null
        if (file.readText().isEmpty()) return null
        return Json.decodeFromString<T>(file.readText())
    }
    inline fun <reified T: Any> write (baseFile: File, path: String, data: T) {
        val file = File(baseFile, path)
        if (!file.exists()) { file.parentFile?.mkdirs(); file.createNewFile() }
        file.writeText(Json.encodeToString(data))
    }
    fun delete (baseFile: File, path: String) {
        File(baseFile, path).delete()
    }

}

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

class ObjectPersist<T> (screen: Screen<*>, key: String, defaultValue: T) : PersistState<T>(screen, key, defaultValue) {
    private val internalDirectory = File(screen.activity.filesDir, javaClass.simpleName)

    override fun invoke() = LocalDB.read(internalDirectory, key)?:defaultValue!!
    override fun invoke(value: T) { if(value == null) LocalDB.delete(internalDirectory, key) else LocalDB.write(internalDirectory, key, value) }

}

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
