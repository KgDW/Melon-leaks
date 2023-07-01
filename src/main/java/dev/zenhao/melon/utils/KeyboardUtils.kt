package dev.zenhao.melon.utils

import org.lwjgl.input.Keyboard
import java.util.*

object KeyboardUtils {
    val allKeys = IntArray(Keyboard.KEYBOARD_SIZE) { it }

    private val displayNames =
        Array(Keyboard.KEYBOARD_SIZE) {
            Keyboard.getKeyName(it)?.lowercase()?.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
            }
        }

    private val keyMap: Map<String, Int> =
        HashMap<String, Int>().apply {
            // LWJGL names
            for (key in 0 until Keyboard.KEYBOARD_SIZE) {
                val name = Keyboard.getKeyName(key) ?: continue
                this[name.lowercase(Locale.ROOT)] = key
            }

            // Display names
            for ((index, name) in displayNames.withIndex()) {
                if (name == null) continue
                this[name.lowercase(Locale.ROOT)] = index
            }

            // Modifier names
            this["ctrl"] = Keyboard.KEY_LCONTROL
            this["alt"] = Keyboard.KEY_LMENU
            this["shift"] = Keyboard.KEY_LSHIFT
            this["meta"] = Keyboard.KEY_LMETA
        }

//    fun sendUnknownKeyError(bind: String) {
//        MessageSendUtils.sendNoSpamWarningMessage("Unknown key [$bind]!")
//    }

    fun getKey(keyName: String): Int {
        return keyMap[keyName.lowercase(Locale.ROOT)] ?: 0
    }

    fun getKeyName(keycode: Int): String? {
        return Keyboard.getKeyName(keycode)
    }

    fun getDisplayName(keycode: Int): String? {
        return displayNames.getOrNull(keycode)
    }
}
