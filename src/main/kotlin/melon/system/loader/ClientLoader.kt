package melon.system.loader

import kotlinx.coroutines.runBlocking
import melon.system.command.CommandManager
import melon.system.manager.ManagerLoader
import melon.system.module.ModuleManager

object ClientLoader {
    private val loaderList = ArrayList<AsyncLoader<*>>()

    init {
        loaderList.add(ModuleManager)
        loaderList.add(CommandManager)
        loaderList.add(ManagerLoader)
        //loaderList.add(GuiManager)
    }

    @JvmStatic
    fun preLoadAll() {
        loaderList.forEach { it.preLoad() }
    }

    @JvmStatic
    fun loadAll() {
        runBlocking { loaderList.forEach { it.load() } }
    }
}
