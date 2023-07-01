package melon.system.config

import dev.zenhao.melon.Melon
import melon.system.config.basic.IConfig
import melon.system.config.configs.GenericConfig
import melon.system.config.configs.GuiConfig
import melon.system.config.configs.ModuleConfig
import melon.system.util.collections.NameableSet

private val configSet = NameableSet<IConfig>()

internal object ConfigManager {


    init {
        register(GuiConfig)
        register(ModuleConfig)
    }

    fun loadAll(): Boolean {
        var success = load(GenericConfig) // Generic config must be loaded first

        configSet.forEach { success = load(it) || success }

        return success
    }

    fun load(config: IConfig): Boolean {
        return try {
            config.load()
            Melon.logger.info("${config.name} config loaded")
            true
        } catch (e: Exception) {
            Melon.logger.error("Failed to load ${config.name} config", e)
            false
        }
    }

    fun saveAll(): Boolean {
        var success = save(GenericConfig) // Generic config must be loaded first

        configSet.forEach { success = save(it) || success }

        return success
    }

    fun save(config: IConfig): Boolean {
        return try {
            config.save()
            Melon.logger.info("${config.name} config saved")
            true
        } catch (e: Exception) {
            Melon.logger.error("Failed to save ${config.name} config!", e)
            false
        }
    }

    fun register(config: IConfig) {
        configSet.add(config)
    }

    fun unregister(config: IConfig) {
        configSet.remove(config)
    }
}