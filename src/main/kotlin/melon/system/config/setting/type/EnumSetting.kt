package melon.system.config.setting.type

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import melon.system.config.setting.AbstractSetting
import dev.zenhao.melon.utils.asStringOrNull
import dev.zenhao.melon.utils.extension.next
import java.util.*

class EnumSetting<T : Enum<T>>(
    name: String,
    value: T,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: T, input: T) -> T = { _, input -> input },
    description: String = ""
) : AbstractSetting<T>(name, value, visibility, consumer, description) {

    val enumClass: Class<T> = value.declaringJavaClass
    val enumValues: Array<out T> = enumClass.enumConstants

    fun nextValue() {
        value = value.next()
    }

    override fun read(valueIn: String) {
        super.read(valueIn.uppercase(Locale.ROOT).replace(' ', '_'))
    }

    override fun write(): JsonElement = JsonPrimitive(value.name)

    override fun read(jsonElement: JsonElement) {
        jsonElement.asStringOrNull?.let { element ->
            enumValues.firstOrNull { it.name.equals(element, true) }?.let {
                value = it
            }
        }
    }

}