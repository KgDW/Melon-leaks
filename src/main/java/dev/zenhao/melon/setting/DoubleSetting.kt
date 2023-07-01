package dev.zenhao.melon.setting

import dev.zenhao.melon.module.IModule
import java.util.function.Predicate

class DoubleSetting(name: String, contain: IModule, defaultValue: Double, var min: Double, var max: Double, var modify: Double = 0.0) :
    Setting<Double>(name, contain, defaultValue) {
    override fun v(predicate: Predicate<Any>): DoubleSetting {
        return super.v(predicate) as DoubleSetting
    }

    override fun setOnChange(listener: onChangeListener<Double>): Setting<Double> {
        return super.setOnChange(listener) as DoubleSetting
    }

    fun b(value: BooleanSetting): DoubleSetting {
        return super.v { v: Any -> value.value } as DoubleSetting
    }

    fun bn(value: BooleanSetting): DoubleSetting {
        return super.v { v: Any -> !value.value } as DoubleSetting
    }

    fun r(value: BooleanSetting): DoubleSetting {
        return super.v { v: Any -> value.value == false } as DoubleSetting
    }

    fun c(min: Double, setting: Setting<*>, max: Double): DoubleSetting {
        if (setting is IntegerSetting) {
            return super.v { v: Any ->
                setting.value!!
                    .toInt().toDouble() <= max && setting.value!!.toInt().toDouble() >= min
            } as DoubleSetting
        }
        if (setting is FloatSetting) {
            return super.v { v: Any ->
                setting.value!!
                    .toFloat().toDouble() <= max && setting.value!!.toFloat().toDouble() >= min
            } as DoubleSetting
        }
        return if (setting is DoubleSetting) {
            super.v { v: Any -> setting.value!! <= max && setting.value!! >= min } as DoubleSetting
        } else super.v { v: Any -> true } as DoubleSetting
    }

    fun c(min: Double, setting: Setting<*>): DoubleSetting {
        if (setting is IntegerSetting) {
            return super.v { v: Any ->
                setting.value!!
                    .toInt().toDouble() >= min
            } as DoubleSetting
        }
        if (setting is FloatSetting) {
            return super.v { v: Any ->
                setting.value!!
                    .toFloat().toDouble() >= min
            } as DoubleSetting
        }
        return if (setting is DoubleSetting) {
            super.v { v: Any -> setting.value!! >= min } as DoubleSetting
        } else super.v { v: Any -> true } as DoubleSetting
    }

    fun c(setting: Setting<*>, max: Double): DoubleSetting {
        if (setting is IntegerSetting) {
            return super.v { v: Any ->
                setting.value!!
                    .toInt().toDouble() <= max
            } as DoubleSetting
        }
        if (setting is FloatSetting) {
            return super.v { v: Any ->
                setting.value!!
                    .toFloat().toDouble() <= max
            } as DoubleSetting
        }
        return if (setting is DoubleSetting) {
            super.v { v: Any -> setting.value!! <= max } as DoubleSetting
        } else super.v { v: Any -> true } as DoubleSetting
    }

    fun m(value: ModeSetting<*>, mode: Enum<*>): DoubleSetting {
        visibility.add(Predicate { v: Any -> value.value === mode })
        return this
    }
}