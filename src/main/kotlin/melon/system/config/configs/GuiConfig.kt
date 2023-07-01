package melon.system.config.configs

import dev.zenhao.melon.Melon
import melon.system.config.basic.AbstractConfig
import melon.system.config.setting.AbstractSetting
import melon.system.config.setting.SettingRegister
import melon.system.render.component.Component
import java.io.File

internal object GuiConfig : AbstractConfig<Component>(
    "gui",
    Melon.Companion.ConfigPath.GUI
), SettingRegister<Component> {
    override val file get() = File("$filePath/gui.json")
    override val backup get() = File("$filePath/gui.bak")

    override fun addSettingToConfig(owner: Component, setting: AbstractSetting<*>) {
        val groupName = owner.settingGroup.groupName
        if (groupName.isNotEmpty()) {
            getGroupOrPut(groupName).getGroupOrPut(owner.name).addSetting(setting)
        }
    }
}