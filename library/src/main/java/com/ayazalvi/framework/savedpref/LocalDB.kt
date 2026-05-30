package com.ayazalvi.framework.savedpref

import kotlinx.serialization.json.Json
import java.io.File

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
