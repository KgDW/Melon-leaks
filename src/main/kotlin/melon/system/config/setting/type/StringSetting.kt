package melon.system.config.setting.type

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import melon.system.config.setting.AbstractSetting
import dev.zenhao.melon.utils.asStringOrNull

sealed class AbstractCharSequenceSetting<T : CharSequence>(
    name: String,
    value: T,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: T, input: T) -> T = { _, input -> input },
    description: String = ""
) : AbstractSetting<T>(name, value, visibility, consumer, description) {
    val stringValue
        get() = value.toString()

    override fun write() = JsonPrimitive(value.toString())

    override fun read(jsonElement: JsonElement) {
        jsonElement.asStringOrNull?.let { read(it) }
    }
}

class CharSequenceSetting(
    name: String,
    value: CharSequence,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: CharSequence, input: CharSequence) -> CharSequence = { _, input -> input },
    description: String = ""
) : AbstractCharSequenceSetting<CharSequence>(name, value, visibility, consumer, description) {
    override fun read(valueIn: String) {
        value = valueIn
    }
}

class StringSetting(
    name: String,
    value: String,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: String, input: String) -> String = { _, input -> input },
    description: String = ""
) : AbstractCharSequenceSetting<String>(name, value, visibility, consumer, description) {
    override fun read(valueIn: String) {
        value = valueIn
    }
}