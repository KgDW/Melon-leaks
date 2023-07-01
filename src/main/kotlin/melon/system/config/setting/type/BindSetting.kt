package melon.system.config.setting.type

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import melon.system.config.setting.AbstractSetting
import dev.zenhao.melon.utils.Bind
import dev.zenhao.melon.utils.KeyboardUtils
import it.unimi.dsi.fastutil.ints.IntArrayList
import melon.system.event.AlwaysListening
import melon.system.util.interfaces.MinecraftWrapper
import melon.system.util.interfaces.Nameable
import java.util.concurrent.CopyOnWriteArrayList

class BindSetting(
    name: String,
    value: Bind,
    visibility: ((() -> Boolean))? = null,
    private val action: ((Boolean) -> Unit)? = null,
    description: String = ""
) : AbstractSetting<Bind>(name, value, visibility, { _, input -> input }, description) {

    init {
        binds.add(this)
    }

    override fun resetValue() {
        value.setBind(defaultValue.modifierKeys, defaultValue.key)
    }

    override fun read(valueIn: String) {
        if (valueIn.equals("None", ignoreCase = true)) {
            value.clear()
            return
        }

        val splitNames = valueIn.split('+')
        val lastName = splitNames.last()
        val lastKey = if (!lastName.startsWith("Mouse ")) {
            KeyboardUtils.getKey(splitNames.last())
        } else {
            lastName.last().digitToIntOrNull()?.let {
                -it - 1
            } ?: 0
        }

        // Don't clear if the string is fucked
        if (lastKey !in 1..255 && lastKey !in -16..-1) {
            return
        }

        val modifierKeys = IntArrayList(0)
        for (index in 0 until splitNames.size - 1) {
            val name = splitNames[index]
            val key = KeyboardUtils.getKey(name)

            if (key !in 1..255) continue
            modifierKeys.add(key)
        }

        value.setBind(modifierKeys, lastKey)
    }

    override fun write() = JsonPrimitive(value.toString())

    override fun read(jsonElement: JsonElement) {
        read(jsonElement.asString ?: "None")
    }

    companion object : AlwaysListening, MinecraftWrapper, Nameable {
        override val name: String = "BindSetting"
        private val binds = CopyOnWriteArrayList<BindSetting>()

        init {
//            listener<InputEvent.Keyboard>(true) {
//                if (mc.currentScreen == null && it.key != Keyboard.KEY_NONE && !Keyboard.isKeyDown(Keyboard.KEY_F3)) {
//                    for (bind in binds) {
//                        if (bind.value.isDown(it.key)) {
//                            bind.action?.invoke(it.state)
//                        }
//                    }
//                }
//            }
//
//            listener<InputEvent.Mouse>(true) {
//                if (mc.currentScreen == null) {
//                    for (bind in binds) {
//                        if (bind.value.isDown(-it.button - 1)) bind.action?.invoke(it.state)
//                    }
//                }
//            }
        }
    }
}