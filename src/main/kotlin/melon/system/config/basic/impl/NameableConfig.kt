package melon.system.config.basic.impl

import melon.system.config.basic.AbstractConfig
import melon.system.config.setting.AbstractSetting
import melon.system.util.interfaces.Nameable
import java.nio.file.Path

open class NameableConfig<T : Nameable>(
    name: String,
    filePath: Path
) : AbstractConfig<T>(name, filePath) {

    override fun addSettingToConfig(owner: T, setting: AbstractSetting<*>) {
        getGroupOrPut(owner.name).addSetting(setting)
    }
}