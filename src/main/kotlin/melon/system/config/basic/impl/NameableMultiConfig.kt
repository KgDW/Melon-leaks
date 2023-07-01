package melon.system.config.basic.impl

import melon.system.config.basic.AbstractMultiConfig
import melon.system.config.setting.AbstractSetting
import melon.system.util.interfaces.Nameable
import java.nio.file.Path

open class NameableMultiConfig<T : Nameable>(
    name: String,
    filePath: Path
) : AbstractMultiConfig<T>(name, filePath) {
    override fun addSettingToConfig(owner: T, setting: AbstractSetting<*>) {
        getGroupOrPut(owner.name).addSetting(setting)
    }
}