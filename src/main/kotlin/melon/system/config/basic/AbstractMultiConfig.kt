package melon.system.config.basic

import com.google.gson.JsonObject
import dev.zenhao.melon.Melon
import melon.system.config.setting.SettingRegister
import melon.system.config.setting.group.SettingGroup
import melon.system.config.setting.group.SettingMultiGroup
import dev.zenhao.melon.utils.JsonUtils
import dev.zenhao.melon.utils.TimeUnit
import dev.zenhao.melon.utils.delegate.CachedValue
import java.io.File
import java.nio.file.Path

abstract class AbstractMultiConfig<T : Any>(
    name: String,
    protected val directoryPath: Path,
    vararg groupNames: String
) : AbstractConfig<T>(name, directoryPath), IConfig, SettingRegister<T> {
    override val file: File get() = File("$directoryPath/$name")
    override val backup: File get() = File("$directoryPath/$name/$name-thin.bak")

    var thinBackup = true

    private val cacheThin = mutableMapOf<File, CachedValue<JsonObject>>()

    init {
        for (groupName in groupNames) addGroup(SettingMultiGroup(groupName))
    }

    override fun save() {
        if (!file.exists()) file.mkdirs()

        for (group in subGroup.values) {
            val file = getFiles(group)
            saveToFile(group, file.first, if (thinBackup) null else file.second)
        }

        if (thinBackup) {
            saveThinBackup()
        }
    }

    override fun load() {
        if (!file.exists()) file.mkdirs()

        for (group in subGroup.values) {
            val file = getFiles(group)
            try {
                loadFromFile(group, file.first)
            } catch (e: Exception) {
                Melon.logger.warn("Failed to load latest, loading backup.")
                if (thinBackup) {
                    loadThinBackup(group)
                } else {
                    loadFromFile(group, file.second)
                }

            }
        }
    }

    private fun loadThinBackup(group: SettingGroup) = loadThin(group, backup)

    private fun saveThinBackup() = saveThin(backup)


    fun saveThin(file: File) {
        JsonUtils.fixEmptyJson(file)

        val toSave = JsonObject().apply {
            for (group in subGroup.values) {
                add(group.name, group.write())
            }
        }

        file.bufferedWriter().use {
            JsonUtils.gson.toJson(toSave, it)
        }

        cacheThin[file]?.update()
    }


    fun loadThin(group: SettingGroup, file: File) {
        if (!file.exists()) file.mkdirs()
        JsonUtils.fixEmptyJson(file)

        cacheThin.getOrPut(file) {
            CachedValue(10L, TimeUnit.SECONDS) {
                file.bufferedReader().use {
                    JsonUtils.parser.parse(it).asJsonObject
                }
            }
        }.get().let { it.get(group.name)?.let { je -> group.read(je as JsonObject) } }
    }

    fun loadThin(file: File) {
        if (!file.exists()) file.mkdirs()
        JsonUtils.fixEmptyJson(file)

        cacheThin.getOrPut(file) {
            CachedValue(10L, TimeUnit.SECONDS) {
                file.bufferedReader().use {
                    JsonUtils.parser.parse(it).asJsonObject
                }
            }
        }.get().let {
            for (group in subGroup.values) {
                it.get(group.name)?.let { je -> group.read(je as JsonObject) }
            }
        }
    }

    /**
     * Get the file pair for a group
     *
     * @param group Group to get the file pair
     *
     * @return Pair of this group's main file to its backup file
     */
    private fun getFiles(group: SettingMultiGroup) = File("${file.path}/${group.name}.json") to File("${file.path}/${group.name}.bak")
}