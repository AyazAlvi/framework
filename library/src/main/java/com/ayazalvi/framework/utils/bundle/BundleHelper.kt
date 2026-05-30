package com.ayazalvi.framework.utils.bundle

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable

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
