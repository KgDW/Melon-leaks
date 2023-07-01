package dev.zenhao.melon.setting

import dev.zenhao.melon.module.IModule
import java.util.function.Predicate

class IntegerSetting(name: String, contain: IModule, defaultValue: Int, var min: Int, var max: Int, var modify: Int = 0) :
    Setting<Int>(name, contain, defaultValue) {
    override fun v(predicate: Predicate<Any>): IntegerSetting {
        return super.v(predicate) as IntegerSetting
    }

    override fun setOnChange(listener: onChangeListener<Int>): Setting<Int> {
        return super.setOnChange(listener) as IntegerSetting
    }

    fun b(value: Boolean): IntegerSetting {
        return super.v { v: Any -> value } as IntegerSetting
    }

    fun b(value: BooleanSetting): IntegerSetting {
        return super.v { v: Any -> value.value } as IntegerSetting
    }

    fun bn(value: BooleanSetting): IntegerSetting {
        return super.v { v: Any -> !value.value } as IntegerSetting
    }

    fun r(value: BooleanSetting): IntegerSetting {
        return super.v { v: Any -> !value.value } as IntegerSetting
    }

    fun c(min: Double, setting: Setting<*>, max: Double): IntegerSetting {
        if (setting is IntegerSetting) {
            return super.v { v: Any ->
                setting.value!!
                    .toDouble() <= max && setting.value!!.toInt().toDouble() >= min
            } as IntegerSetting
        }
        if (setting is FloatSetting) {
            return super.v { v: Any ->
                setting.value.toDouble() <= max && setting.value.toFloat().toDouble() >= min
            } as IntegerSetting
        }
        return if (setting is DoubleSetting) {
            super.v { v: Any -> setting.value <= max && setting.value >= min } as IntegerSetting
        } else super.v { v: Any -> true } as IntegerSetting
    }

    fun c(min: Double, setting: Setting<*>): IntegerSetting {
        if (setting is IntegerSetting) {
            return super.v { v: Any ->
                setting.value!!
                    .toDouble() >= min
            } as IntegerSetting
        }
        if (setting is FloatSetting) {
            return super.v { v: Any -> setting.value.toDouble() >= min } as IntegerSetting
        }
        return if (setting is DoubleSetting) {
            super.v { v: Any -> setting.value >= min } as IntegerSetting
        } else super.v { v: Any -> true } as IntegerSetting
    }

    fun c(setting: Setting<*>, max: Double): IntegerSetting {
        if (setting is IntegerSetting) {
            return super.v { v: Any ->
                setting.value!!
                    .toDouble() <= max
            } as IntegerSetting
        }
        if (setting is FloatSetting) {
            return super.v { v: Any -> setting.value.toDouble() <= max } as IntegerSetting
        }
        return if (setting is DoubleSetting) {
            super.v { v: Any -> setting.value <= max } as IntegerSetting
        } else super.v { v: Any -> true } as IntegerSetting
    }

    fun m(value: ModeSetting<*>, mode: Enum<*>): IntegerSetting {
        visibility.add(Predicate { v: Any -> value.value === mode })
        return this
    }

    fun no(value: ModeSetting<*>, mode: Enum<*>): IntegerSetting {
        visibility.add(Predicate { v: Any -> value.value !== mode })
        return this
    }
}