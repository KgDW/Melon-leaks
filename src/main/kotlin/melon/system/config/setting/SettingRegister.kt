package melon.system.config.setting

import dev.zenhao.melon.utils.Bind
import melon.system.config.setting.type.*
import melon.system.util.color.ColorRGB

/**
 * Setting register overloading
 *
 * @param T Type to have extension function for registering setting
 */
interface SettingRegister<T : Any> {

    /** Integer Setting */
    fun T.setting(
        name: String,
        value: Int,
        range: IntRange,
        step: Int,
        visibility: ((() -> Boolean))? = null,
        consumer: (prev: Int, input: Int) -> Int = { _, input -> input },
        description: String = "",
        fineStep: Int = step,
    ) = setting(IntegerSetting(name, value, range, step, visibility, consumer, description, fineStep))

    /** Double Setting */
    fun T.setting(
        name: String,
        value: Double,
        range: ClosedFloatingPointRange<Double>,
        step: Double,
        visibility: ((() -> Boolean))? = null,
        consumer: (prev: Double, input: Double) -> Double = { _, input -> input },
        description: String = "",
        fineStep: Double = step,
    ) = setting(DoubleSetting(name, value, range, step, visibility, consumer, description, fineStep))

    /** Float Setting */
    fun T.setting(
        name: String,
        value: Float,
        range: ClosedFloatingPointRange<Float>,
        step: Float,
        visibility: ((() -> Boolean))? = null,
        consumer: (prev: Float, input: Float) -> Float = { _, input -> input },
        description: String = "",
        fineStep: Float = step,
    ) = setting(FloatSetting(name, value, range, step, visibility, consumer, description, fineStep))

    /** Bind Setting */
    fun T.setting(
        name: String,
        value: Bind,
        action: ((Boolean) -> Unit)? = null,
        visibility: ((() -> Boolean))? = null,
        description: String = ""
    ) = setting(melon.system.config.setting.type.BindSetting(name, value, visibility, action, description))

    /** Color Setting */
    fun T.setting(
        name: String,
        value: ColorRGB,
        hasAlpha: Boolean = true,
        visibility: ((() -> Boolean))? = null,
        description: String = ""
    ) = setting(melon.system.config.setting.type.ColorSetting(name, value, hasAlpha, visibility, description))

    /** Boolean Setting */
    fun T.setting(
        name: String,
        value: Boolean,
        visibility: ((() -> Boolean))? = null,
        consumer: (prev: Boolean, input: Boolean) -> Boolean = { _, input -> input },
        description: String = ""
    ) = setting(BooleanSetting(name, value, visibility, consumer, description))

    /** Enum Setting */
    fun <E : Enum<E>> T.setting(
        name: String,
        value: E,
        visibility: (() -> Boolean)? = null,
        consumer: (prev: E, input: E) -> E = { _, input -> input },
        description: String = ""
    ) = setting(EnumSetting(name, value, visibility, consumer, description))

    /** String Setting */
    fun T.setting(
        name: String,
        value: String,
        visibility: (() -> Boolean)? = null,
        consumer: (prev: String, input: String) -> String = { _, input -> input },
        description: String = ""
    ) = setting(StringSetting(name, value, visibility, consumer, description))
    /* End of setting registering */

    /**
     * Register a setting
     *
     * @param S Type of the setting
     * @param setting Setting to register
     *
     * @return [setting]
     */
    fun <S : AbstractSetting<*>> T.setting(setting: S): S
}