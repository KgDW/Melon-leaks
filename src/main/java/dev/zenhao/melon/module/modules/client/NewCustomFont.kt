package dev.zenhao.melon.module.modules.client

import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.utils.animations.fastCeil
import melon.events.TickEvent
import melon.system.event.listener
import melon.utils.TimeUnit
import melon.utils.concurrent.threads.onMainThread
import melon.system.util.delegate.AsyncCachedValue
import melon.system.render.font.GlyphCache
import melon.system.render.font.renderer.MainFontRenderer
import java.awt.Font
import java.awt.GraphicsEnvironment
import java.util.*

@Module.Info(name = "NewCustomFont", category = Category.CLIENT)
internal object NewCustomFont : Module() {

    private const val DEFAULT_FONT_NAME = "Lexend Deca"
    val overrideMinecraft = bsetting("Override Minecraft", false)
    private var reloadFont = bsetting("ReloadFont", false)
    private val sizeSetting = fsetting("Size", 1.0f, 0.5f, 2.0f)
    private val charGapSetting = fsetting("CharGap", 0.0f, -10f, 10f)
    private val lineSpaceSetting = fsetting("LineSpace", 0.0f, -10f, 10f)
    private val baselineOffsetSetting = fsetting("BaselineOffset", 0.0f, -10.0f, 10.0f)
    private val lodBiasSetting = fsetting("LodBias", 0.0f, -10.0f, 10.0f)

    val size get() = sizeSetting.value * 0.1425f
    val charGap get() = charGapSetting.value * 0.5f - 2.05f
    val lineSpace get() = size * (lineSpaceSetting.value * 0.05f + 0.77f)
    val lodBias get() = lodBiasSetting.value * 0.25f - 0.5375f
    val baselineOffset get() = baselineOffsetSetting.value * 2.0f - 8.0f

    init {
        listener<TickEvent.Post>(true) {
            mc.fontRenderer.FONT_HEIGHT = if (overrideMinecraft.value) {
                MainFontRenderer.getHeight().fastCeil()
            } else {
                9
            }
            if (reloadFont.value) {
                GlyphCache.delete(Font(DEFAULT_FONT_NAME, Font.PLAIN, 64))
                onMainThread {
                    MainFontRenderer.reloadFonts()
                }
                reloadFont.value = false
            }
        }
    }

    /** Available fonts on the system */
    val availableFonts: Map<String, String> by AsyncCachedValue(5L, TimeUnit.SECONDS) {
        HashMap<String, String>().apply {
            val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()

            environment.availableFontFamilyNames.forEach {
                this[it.lowercase(Locale.ROOT)] = it
            }

            environment.allFonts.forEach {
                val family = it.family
                if (family != Font.DIALOG) {
                    this[it.name.lowercase(Locale.ROOT)] = family
                }
            }
        }
    }
}