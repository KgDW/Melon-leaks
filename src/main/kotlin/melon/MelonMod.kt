package melon

import dev.zenhao.melon.command.CommandManager
import dev.zenhao.melon.command.commands.module.ConfigCommand
import dev.zenhao.melon.event.ForgeEventProcessor
import dev.zenhao.melon.gui.clickgui.GUIRender
import dev.zenhao.melon.gui.clickgui.HUDRender
import dev.zenhao.melon.gui.settingpanel.MelonSettingPanel
import dev.zenhao.melon.manager.*
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.module.modules.client.SettingPanel
import dev.zenhao.melon.module.modules.player.Freecam
import dev.zenhao.melon.utils.ClassInvoke
import dev.zenhao.melon.utils.Crasher
import dev.zenhao.melon.utils.NetworkDump
import dev.zenhao.melon.utils.java.mkdirIfNotExists
import dev.zenhao.melon.utils.math.deneb.LagCompensator
import melon.system.antileak.AntiLeak
import melon.system.config.ConfigUtils
import melon.system.event.SafeClientEvent
import melon.system.loader.ClientLoader
import melon.system.render.font.renderer.MainFontRenderer
import melon.utils.threads.BackgroundScope
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
/*
class MelonMod {
    @JvmField
    var friendManager: FriendManager? = null
    var commandManager: CommandManager? = null
    var guiRender: GUIRender? = null
    var hudEditor: HUDRender? = null
    var guiManager: GuiManager? = null

    @Mod.EventHandler
    fun onPreInit(event: FMLPreInitializationEvent?) {
        ClassInvoke.INSTANCE.addClass(NetworkDump::class.java, "Dump")
        Thread.currentThread().priority = Thread.MAX_PRIORITY
        ConfigCommand.org = false
        Melon.setTitleAndIcon()
        if (Package.getPackage("club.eridani.epsilon") != null) {
            Crasher()
        }
        LagCompensator.INSTANCE = LagCompensator()
        SafeClientEvent.initListener()

        Melon.Mod.DIRECTORY.toFile().mkdirIfNotExists()

        AntiLeak.checkAll()

        ClientLoader.preLoadAll()

        Thread.currentThread().priority = Thread.MAX_PRIORITY
    }

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent?) {
        Melon.logger.info("Initializing ${Melon.NAME} ${Melon.VERSION}")

        ClientLoader.loadAll()
        MinecraftForge.EVENT_BUS.register(ForgeEventProcessor)
        ConfigUtils.loadAll()
        BackgroundScope.start()
        MainFontRenderer.reloadFonts()

        Melon.logger.info("${Melon.NAME} initialized!")
    }

    @Mod.EventHandler
    fun onPostInit(event: FMLPostInitializationEvent?) {
        ready = true
        MinecraftForge.EVENT_BUS.register(BackdoorManager)

        SettingPanel.INSTANCE.setGUIScreen(MelonSettingPanel())

        if (ModuleManager.getModuleByClass(Freecam::class.java).isEnabled) {
            Freecam.INSTANCE!!.disable()
        }

        Runtime.getRuntime().addShutdownHook(Thread {
            try {
                BackdoorManager.close()
            } catch (_: Exception) {}
        })

        System.gc()
    }

    companion object {
        var ready = false; private set
    }
}
*/