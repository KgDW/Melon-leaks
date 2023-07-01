package melon.system.config.setting.type

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import melon.system.config.setting.AbstractSetting
import dev.zenhao.melon.utils.asBooleanOrNull

open class BooleanSetting(
    name: String,
    value: Boolean,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: Boolean, input: Boolean) -> Boolean = { _, input -> input },
    description: String = ""
) : AbstractSetting<Boolean>(name, value, visibility, consumer, description) {
    override fun write(): JsonElement = JsonPrimitive(value)

    override fun read(jsonElement: JsonElement) {
        jsonElement.asBooleanOrNull?.let { value = it }
    }


}