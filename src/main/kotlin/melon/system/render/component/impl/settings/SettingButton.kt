package melon.system.render.component.impl.settings

import melon.system.config.setting.type.BooleanSetting
import melon.system.render.component.impl.basic.BooleanSlider
import dev.zenhao.melon.utils.vector.Vec2f

class SettingButton(val setting: BooleanSetting) : BooleanSlider(setting.name, setting.description, setting.visibility) {
    override val progress: Float
        get() = if (setting.value) 1.0f else 0.0f

    override fun onClick(mousePos: Vec2f, buttonId: Int) {
        super.onClick(mousePos, buttonId)
        setting.value = !setting.value
    }
}