package melon.system.config.setting

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import melon.system.util.interfaces.Nameable
import kotlin.reflect.KProperty

abstract class AbstractSetting<T : Any>(
    override val name: String,
    val defaultValue: T,
    val visibility: ((() -> Boolean))? = null,
    consumer: (prev: T, input: T) -> T,
    val description: String
) : Nameable {
    var value: T = defaultValue
        set(value) {
            if (value != field) {
                val prev = field
                var new = value

                for (index in consumers.size - 1 downTo 0) {
                    new = consumers[index](prev, new)
                }
                field = new

                valueListeners.forEach { it(prev, field) }
                listeners.forEach { it() }
            }
        }

    val valueClass: Class<T> = defaultValue.javaClass
    val isVisible get() = visibility?.invoke() ?: true
    val isModified get() = this.value != this.defaultValue

    val listeners = ArrayList<() -> Unit>()
    val valueListeners = ArrayList<(prev: T, input: T) -> Unit>()

    val consumers = ArrayList<(prev: T, input: T) -> T>().apply {
        add(consumer)
    }

    open fun resetValue() {
        value = defaultValue
    }

    //Json
    open fun write(): JsonElement = gson.toJsonTree(value)

    open fun read(jsonElement: JsonElement) {
        value = gson.fromJson(jsonElement, valueClass)
    }

    open fun read(valueIn: String) = read(parser.parse(valueIn))

    // Kotlin Get Set

    operator fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return value
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
    }

    override fun toString() = value.toString()

    override fun equals(other: Any?) =
        this === other ||
                (other is AbstractSetting<*> &&
                        this.valueClass == other.valueClass &&
                        this.name == other.name &&
                        this.value == other.value)

    override fun hashCode() = valueClass.hashCode() * 31 + name.hashCode() * 31 + value.hashCode()

    protected companion object {
        val gson: Gson = GsonBuilder().setPrettyPrinting().create()
        val parser = JsonParser()
    }
}