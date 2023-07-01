package dev.zenhao.melon.setting

import dev.zenhao.melon.module.IModule
import java.util.function.Predicate

class FloatSetting(name: String, contain: IModule, defaultValue: Float, var min: Float, var max: Float, var modify: Float = 0f) :
    Setting<Float>(name, contain, defaultValue) {
    override fun v(predicate: Predicate<Any>): FloatSetting {
        return super.v(predicate) as FloatSetting
    }

    override fun setOnChange(listener: onChangeListener<Float>): Setting<Float> {
        return super.setOnChange(listener) as FloatSetting
    }

    fun b(value: BooleanSetting): FloatSetting {
        return super.v { v: Any -> value.value } as FloatSetting
    }

    fun or(value: BooleanSetting, value2: BooleanSetting): FloatSetting {
        return super.v { value.value || value2.value } as FloatSetting
    }

    fun b2(value: BooleanSetting): FloatSetting {
        return super.v { v: Any -> !value.value } as FloatSetting
    }

    fun r(value: BooleanSetting): FloatSetting {
        return super.v { v: Any -> value.value == false } as FloatSetting
    }

    fun c(min: Double, setting: Setting<*>, max: Double): FloatSetting {
        if (setting is IntegerSetting) {
            return super.v { v: Any ->
                setting.value!!.toInt().toDouble() <= max && setting.value!!.toInt().toDouble() >= min
            } as FloatSetting
        }
        if (setting is FloatSetting) {
            return super.v { v: Any ->
                setting.value!!.toFloat().toDouble() <= max && setting.value!!.toFloat().toDouble() >= min
            } as FloatSetting
        }
        return if (setting is DoubleSetting) {
            super.v { v: Any -> setting.value as Double <= max && setting.value as Double >= min } as FloatSetting
        } else super.v { v: Any -> true } as FloatSetting
    }

    fun c(min: Double, setting: Setting<*>): FloatSetting {
        if (setting is IntegerSetting) {
            return super.v { v: Any -> setting.value!!.toInt().toDouble() >= min } as FloatSetting
        }
        if (setting is FloatSetting) {
            return super.v { v: Any -> setting.value!!.toFloat().toDouble() >= min } as FloatSetting
        }
        return if (setting is DoubleSetting) {
            super.v { v: Any -> setting.value as Double >= min } as FloatSetting
        } else super.v { v: Any -> true } as FloatSetting
    }

    fun c(setting: Setting<*>, max: Double): FloatSetting {
        if (setting is IntegerSetting) {
            return super.v { v: Any -> setting.value!!.toInt().toDouble() <= max } as FloatSetting
        }
        if (setting is FloatSetting) {
            return super.v { v: Any -> setting.value!!.toFloat().toDouble() <= max } as FloatSetting
        }
        return if (setting is DoubleSetting) {
            super.v { v: Any -> setting.value as Double <= max } as FloatSetting
        } else super.v { v: Any -> true } as FloatSetting
    }

    fun m(value: ModeSetting<*>, mode: Enum<*>): FloatSetting {
        visibility.add(Predicate { v: Any -> value.value === mode })
        return this
    }

    fun m2(value: ModeSetting<*>, mode: Enum<*>): FloatSetting {
        visibility.add(Predicate { v: Any -> value.value !== mode })
        return this
    }
}