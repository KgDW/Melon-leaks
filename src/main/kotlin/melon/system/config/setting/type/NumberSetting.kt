package melon.system.config.setting.type

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import melon.system.config.setting.AbstractSetting
import dev.zenhao.melon.utils.*

abstract class NumberSetting<T>(
    name: String,
    value: T,
    val range: ClosedRange<T>,
    val step: T,
    visibility: (() -> Boolean)? = null,
    consumer: (prev: T, input: T) -> T,
    description: String = "",
    val fineStep: T
) : AbstractSetting<T>(name, value, visibility, consumer, description)
        where T : Number, T : Comparable<T> {

    override fun write() = JsonPrimitive(value)

    final override fun read(valueIn: String) {
        valueIn.toDoubleOrNull()?.let {
            read(it)
        }
    }

    abstract fun read(valueIn: Double)
}

class DoubleSetting(
    name: String,
    value: Double,
    range: ClosedFloatingPointRange<Double>,
    step: Double,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: Double, input: Double) -> Double = { _, input -> input },
    description: String = "",
    fineStep: Double = step
) : NumberSetting<Double>(name, value, range, step, visibility, consumer, description, fineStep) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement) {
        jsonElement.asDoubleOrNull?.let { value = it }
    }

    override fun read(valueIn: Double) {
        value = valueIn
    }
}

class FloatSetting(
    name: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    step: Float,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: Float, input: Float) -> Float = { _, input -> input },
    description: String = "",
    fineStep: Float = step
) : NumberSetting<Float>(name, value, range, step, visibility, consumer, description, fineStep) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement) {
        jsonElement.asFloatOrNull?.let { value = it }
    }

    override fun read(valueIn: Double) {
        value = valueIn.toFloat()
    }
}

class IntegerSetting(
    name: String,
    value: Int,
    range: IntRange,
    step: Int,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: Int, input: Int) -> Int = { _, input -> input },
    description: String = "",
    fineStep: Int = step
) : NumberSetting<Int>(name, value, range, step, visibility, consumer, description, fineStep) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement) {
        jsonElement.asIntOrNull?.let { value = it }
    }

    override fun read(valueIn: Double) {
        value = valueIn.toInt()
    }
}

class LongSetting(
    name: String,
    value: Long,
    range: ClosedFloatingPointRange<Long>,
    step: Long,
    visibility: ((() -> Boolean))? = null,
    consumer: (prev: Long, input: Long) -> Long = { _, input -> input },
    description: String = "",
    fineStep: Long = step
) : NumberSetting<Long>(name, value, range, step, visibility, consumer, description, fineStep) {

    init {
        consumers.add(0) { _, it ->
            it.coerceIn(range)
        }
    }

    override fun read(jsonElement: JsonElement) {
        jsonElement.asLongOrNull?.let { value = it }
    }

    override fun read(valueIn: Double) {
        value = valueIn.toLong()
    }
}