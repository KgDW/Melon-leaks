package melon.system.config.setting.type

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import melon.system.config.setting.AbstractSetting
import dev.zenhao.melon.utils.asStringOrNull
import melon.system.util.color.ColorRGB
import java.util.*

class ColorSetting(
    name: String,
    value: ColorRGB,
    val hasAlpha: Boolean = true,
    visibility: ((() -> Boolean))? = null,
    description: String = ""
) : AbstractSetting<ColorRGB>(name, value, visibility, { _, input -> if (!hasAlpha) input.alpha(255) else input }, description) {
    override fun read(valueIn: String) {
        val anti0x = valueIn.removePrefix("0x")
        val split = anti0x.split(',')

        if (split.size in 3..4) {
            val r = split[0].toIntOrNull() ?: return
            val g = split[1].toIntOrNull() ?: return
            val b = split[2].toIntOrNull() ?: return
            val a = split.getOrNull(3)?.toIntOrNull() ?: 255

            value = ColorRGB(r, g, b, a)
        } else {
            anti0x.toIntOrNull(16)?.let {
                value = ColorRGB(it)
            }
        }
    }

    override fun write(): JsonPrimitive = JsonPrimitive(value.rgba.toUInt().toString(16).uppercase(Locale.ROOT))


    override fun read(jsonElement: JsonElement) {
        jsonElement.asStringOrNull?.let { read(it.lowercase(Locale.ROOT)) }
    }
}