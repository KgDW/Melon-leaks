package dev.zenhao.melon.utils

import com.google.gson.*
import dev.zenhao.melon.Melon
import java.io.File
import java.io.FileWriter
import java.io.IOException

val JsonElement.asJsonArrayOrNull: JsonArray? get() = (this as? JsonArray)
val JsonElement.asJsonPrimitiveOrNull: JsonPrimitive? get() = (this as? JsonPrimitive)

val JsonElement.asBooleanOrNull: Boolean? get() = runCatching { this.asBoolean }.getOrNull()
val JsonElement.asIntOrNull: Int? get() = runCatching { this.asInt }.getOrNull()
val JsonElement.asFloatOrNull: Float? get() = runCatching { this.asFloat }.getOrNull()
val JsonElement.asDoubleOrNull: Double? get() = runCatching { this.asDouble }.getOrNull()
val JsonElement.asLongOrNull: Long? get() = runCatching { this.asLong }.getOrNull()
val JsonElement.asStringOrNull: String? get() = runCatching { this.asString }.getOrNull()


object JsonUtils {

    /**
     * Contains a gson object and a parser object
     */
    val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    val parser = JsonParser()

    fun fixEmptyJson(vararg files: File?, isArray: Boolean = false) {
        for (file in files) file?.let {fixEmptyJson(it, isArray)}
    }

    fun fixEmptyJson(file: File, isArray: Boolean = false) {
        var empty = false

        if (!file.exists()) {
            file.createNewFile()
            empty = true
        } else if (file.length() <= 8) {
            val string = file.readText()
            empty = string.isBlank() || string.all {
                it == '[' || it == ']' || it == '{' || it == '}' || it == ' ' || it == '\n' || it == '\r'
            }
        }

        if (empty) {
            try {
                FileWriter(file, false).use {
                    it.write(if (isArray) "[]" else "{}")
                }
            } catch (exception: IOException) {
                Melon.logger.warn("Failed fixing empty json", exception)
            }
        }
    }
}