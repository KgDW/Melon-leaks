package dev.zenhao.melon

import dev.zenhao.melon.command.CommandManager
import dev.zenhao.melon.command.commands.module.ConfigCommand
import dev.zenhao.melon.event.ForgeEventProcessor
import dev.zenhao.melon.gui.clickgui.GUIRender
import dev.zenhao.melon.gui.clickgui.HUDRender
import dev.zenhao.melon.gui.settingpanel.MelonSettingPanel
import dev.zenhao.melon.manager.*
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.module.modules.client.SettingPanel
import dev.zenhao.melon.setting.Setting
import dev.zenhao.melon.setting.StringSetting
import dev.zenhao.melon.utils.*
import dev.zenhao.melon.utils.font.CFontRenderer
import dev.zenhao.melon.utils.java.resolve
import dev.zenhao.melon.utils.math.deneb.LagCompensator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import melon.system.event.AlwaysListening
import melon.system.event.SafeClientEvent
import melon.system.render.font.renderer.MainFontRenderer
import melon.utils.concurrent.threads.EventSystemScope
import melon.utils.concurrent.threads.KernelScope
import melon.utils.threads.BackgroundScope
import net.minecraft.client.Minecraft
import net.minecraft.util.Util
import net.minecraft.util.Util.EnumOS
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.Display
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path

@Mod(modid = Melon.MOD_ID, name = Melon.MOD_NAME, version = Melon.VERSION)
class Melon : AlwaysListening {
    @JvmField
    var friendManager: FriendManager? = null
    var commandManager: CommandManager? = null
    private var guiRender: GUIRender? = null
    private var hudEditor: HUDRender? = null
    private var guiManager: GuiManager? = null

    @Mod.EventHandler
    fun onPreInit(event: FMLPreInitializationEvent) {
        ClassInvoke.INSTANCE.addClass(NetworkDump::class.java, "Dump")
        Thread.currentThread().priority = Thread.MAX_PRIORITY
        ConfigCommand.org = false
        setTitleAndIcon()
        //ClassInvoke.INSTANCE.addClass(Task.class, "TaskRunning");
        //ClassInvoke.INSTANCE.addClass(NetworkDump.class, "Dump");
        for (pac in Package.getPackages()) {
            if (pac == null || !pac.name.contains("epsilon") || !pac.name.contains("muffin") || !pac.name.contains("troll") || !pac.name.contains(
                    "moon"
                ) || !pac.name.contains("abhack")
            ) continue
            Crasher()
            break
        }
        LagCompensator.INSTANCE = LagCompensator()
        SafeClientEvent.initListener()
    }

    @Mod.EventHandler
    fun onInit(event: FMLInitializationEvent) {
        ModuleManager.init()
        instance.commandManager = CommandManager()
        instance.friendManager = FriendManager()
        instance.guiManager = GuiManager()
        MinecraftForge.EVENT_BUS.register(ForgeEventProcessor)
        RotationManager.onInit()
        FontManager.onInit()
        instance.guiRender = GUIRender()
        instance.hudEditor = HUDRender()
        FileManager.onInit()
        FileManager.loadAll(ConfigCommand.org)
        InventoryTaskManager.onInit()
        CrystalManager.onInit()
        HotbarManager.onInit()
        EntityManager.onInit()
        CombatManager.onInit()
        TotemPopManager.onInit()
        HealthManager.onInit()
        HoleManager.onInit()
        BackgroundScope.start()

        MainFontRenderer.reloadFonts()

        /*
        for (i in 0..128) {
            EventSystemScope.launch {
                MainFontRenderer.delegate.regularGlyph.getChunk(i.coerceIn(0, 128))
            }
            EventSystemScope.launch {
                MainFontRenderer.delegate.boldGlyph.getChunk(i.coerceIn(0, 128))
            }
        }

         */
    }

    @Mod.EventHandler
    fun onPostInit(event: FMLPostInitializationEvent) {
        SettingPanel.INSTANCE.setGUIScreen(MelonSettingPanel())
        for (module in ModuleManager.getToggleList()) {
            if (module.isDisabled) continue
            module.disable()
        }
        ready = true
        Runtime.getRuntime().addShutdownHook(Thread {
            runCatching {
                BackdoorManager.close()
            }
        })
        System.gc()
    }

    companion object {
        const val MOD_ID = "melon"
        const val MOD_NAME = "Melon"
        const val VERSION = "4.5"
        private const val DISPLAY_NAME = "$MOD_NAME $VERSION"
        const val KANJI = "Melon"
        const val ALT_Encrypt_Key = "Melon-Dev"

        // Root Dir Save
        val DIRECTORY = Path("melon/")

        @JvmField
        val logger: Logger = LogManager.getLogger("Melon")
        var instance = Melon()

        @JvmField
        var commandPrefix: Setting<String> = StringSetting("CommandPrefix", null, ".")

        @JvmStatic
        @get:JvmName("isReady")
        var ready = false; private set

        @JvmStatic
        fun call() {
        }

        @JvmField
        var fontRenderer: CFontRenderer? = null
        fun setTitleAndIcon() {
            if (Minecraft.getMinecraft().player != null) {
                Display.setTitle(DISPLAY_NAME + " " + Minecraft.getMinecraft().player.name)
            } else {
                Display.setTitle(DISPLAY_NAME)
            }
            setIcon()
        }

        object CachePath {
            val CACHE: Path = DIRECTORY resolve "cache/"

            val GLYPHS: Path = CACHE resolve "glyphs/"
        }

        object ConfigPath {
            private val CONFIG: Path = DIRECTORY resolve "config/"

            val MODULE: Path = CONFIG resolve "modules/"
            val GUI: Path = CONFIG resolve "gui/"
        }

        private fun setIcon() {
            if (Util.getOSType() != EnumOS.OSX) {
                try {
                    val inputstream = Melon::class.java.getResourceAsStream("/assets/melon/logo/logo.png")
                    if (inputstream != null) {
                        Display.setIcon(arrayOf(Utils.readImageToBuffer(inputstream)))
                    }
                } catch (e: IOException) {
                    e.stackTrace
                }
            }
        }
    }
}