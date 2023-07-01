package melon

import dev.zenhao.melon.Melon
import dev.zenhao.melon.utils.Utils
import dev.zenhao.melon.utils.java.resolve
import net.minecraft.client.Minecraft
import net.minecraft.util.Util
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.lwjgl.opengl.Display
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path

/*
object Melon {
    // Basic Name
    const val NAME = "Melon"
    const val VERSION = "4.2"

    @JvmStatic
    @get:JvmName("isReady")
    val ready get() = MelonMod.ready

    @JvmField
    val title: String = Display.getTitle()

    @JvmStatic
    val logger: Logger = LogManager.getLogger(NAME.uppercase())

    object Mod {

        // Mod Info
        const val MOD_ID = "melon"
        const val MOD_NAME = NAME
        const val MOD_VERSION = VERSION

        // Display Title
        const val DISPLAY_NAME = "$NAME $VERSION"

        // Chat Info Prefix
        const val KANJI = NAME

        // ALT Encrypt Key
        const val ALT_Encrypt_Key = "Melon-Dev"

        // Root Dir Save
        val DIRECTORY = Path("melon/")

        // Command Default Prefix
        const val DEFAULT_COMMAND_PREFIX = ";"

        object Package {
            const val MANAGER = "melon.managers"
            const val COMMAND = "melon.commands"
            const val MODULE  = "melon.modules"
            const val HUD     = "melon.hud"
        }
    }

    object ConfigPath {
        val CONFIG: Path = Mod.DIRECTORY resolve "config/"

        val MODULE: Path = CONFIG resolve "modules/"
        val GUI: Path = CONFIG resolve "gui/"
    }

    object CachePath {
        val CACHE: Path = Mod.DIRECTORY resolve "cache/"

        val GLYPHS: Path = CACHE resolve "glyphs/"
    }

    fun setTitleAndIcon() {
        if (Minecraft.getMinecraft().player != null) {
            Display.setTitle(Mod.DISPLAY_NAME + " " + Minecraft.getMinecraft().player.name)
        } else {
            Display.setTitle(Mod.DISPLAY_NAME)
        }
        setIcon()
    }

    private fun setIcon() {
        if (Util.getOSType() != Util.EnumOS.OSX) {
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
*/