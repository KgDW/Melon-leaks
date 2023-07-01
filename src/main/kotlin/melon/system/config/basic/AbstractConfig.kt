package melon.system.config.basic

import dev.zenhao.melon.Melon
import melon.system.config.setting.AbstractSetting
import melon.system.config.setting.SettingRegister
import melon.system.config.setting.group.SettingGroup
import melon.system.config.setting.group.SettingMultiGroup
import dev.zenhao.melon.utils.JsonUtils
import melon.system.util.interfaces.Nameable
import java.io.File
import java.nio.file.Path

abstract class AbstractConfig<T : Any>(
    name: String,
    val filePath: Path
) : SettingMultiGroup(name), IConfig, SettingRegister<T> {

    override val file get() = File("$filePath$name.json")
    override val backup get() = File("$filePath$name.bak")

    final override fun <S : AbstractSetting<*>> T.setting(setting: S): S {
        addSettingToConfig(this, setting)
        return setting
    }

    abstract fun addSettingToConfig(owner: T, setting: AbstractSetting<*>)

    open fun getSettings(nameable: Nameable) = getSettings(nameable.name)
    open fun getSettings(name: String) = getGroup(name)?.getSettings() ?: emptyList()


    override fun save() {
        filePath.toFile().run {
            if (!exists()) mkdirs()
        }

        saveToFile(this, file, backup)
    }

    override fun load() {
        try {
            loadFromFile(this, file)
        } catch (e: Exception) {
            Melon.logger.warn("Failed to load latest, loading backup.")
            loadFromFile(this, backup)
        }
    }

    /**
     * Save a group to a file
     *
     * @param group Group to save
     * @param file Main file of [group]'s json
     * @param backup Backup file of [group]'s json
     */
    protected fun saveToFile(group: SettingGroup, file: File, backup: File?) {
        JsonUtils.fixEmptyJson(file, backup)
        if (file.exists()) backup?.let { file.copyTo(it, true) }

        file.bufferedWriter().use {
            JsonUtils.gson.toJson(group.write(), it)
        }
    }

    /**
     * Load settings values of a group
     *
     * @param group Group to load
     * @param file file of [group]'s json
     */
    protected fun loadFromFile(group: SettingGroup, file: File) {
        JsonUtils.fixEmptyJson(file)

        file.bufferedReader().use {
            JsonUtils.parser.parse(it).asJsonObject
        }?.let {
            group.read(it)
        }
    }



}