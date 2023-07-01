package melon.system.render.gui.clickgui.window

import melon.system.config.setting.AbstractSetting
import melon.system.module.AbstractModule
import melon.system.render.component.impl.windows.SettingWindow

class ModuleSettingWindow(
    module: AbstractModule,
    posX: Float,
    posY: Float
) : SettingWindow<AbstractModule>(module.name, module, posX, posY) {
    override fun getSettingList(): List<AbstractSetting<*>> {
        return element.fullSettingList.filter { it.name != "Enabled" && it.name != "Clicks" }
    }

}