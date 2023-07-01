package dev.zenhao.melon.module.hud.huds

import dev.zenhao.melon.event.events.player.PlayerMotionEvent
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.IModule
import dev.zenhao.melon.module.ModuleManager
import dev.zenhao.melon.utils.animations.Easing
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import melon.events.TickEvent
import melon.system.event.safeEventListener
import melon.system.event.safeParallelListener
import melon.system.render.font.TextComponent
import melon.system.render.font.renderer.MainFontRenderer
import melon.system.render.graphic.RenderUtils2D
import melon.system.util.collections.ArrayMap
import melon.system.util.color.ColorRGB
import melon.system.util.color.ColorUtils
import melon.system.util.delegate.AsyncCachedValue
import melon.system.util.state.TimedFlag
import melon.utils.TimeUnit
import melon.utils.extension.sumOfFloat
import melon.utils.graphics.HAlign
import melon.utils.graphics.VAlign
import melon.utils.text.format
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.text.TextFormatting
import kotlin.math.max

@HUDModule.Info(name = "ActiveModules")
object ActiveModules : HUDModule() {
    private val mode0 = msetting("Mode", Mode.LEFT_TAG)
    private var mode = (mode0.value as Mode)
    private val sortingMode0 = msetting("SortingMode", SortingMode.LENGTH)
    private var sortingMode = (sortingMode0.value as SortingMode)
    private val rainbow = bsetting("Rainbow", true)
    private val rainbowLength = fsetting("RainbowLength", 10.0f, 1.0f, 20.0f, 0.5f).b(rainbow)
    private val indexedHue = fsetting("IndexedHue", 0.5f, 0.0f, 1.0f, 0.05f).b(rainbow)
    private val saturation = fsetting("Saturation", 0.5f, 0.0f, 1.0f, 0.01f).b(rainbow)
    private val brightness = fsetting("Brightness", 1.0f, 0.0f, 1.0f, 0.01f).b(rainbow)
    private val frameColor = csetting("FrameColor", ColorRGB(12, 16, 20, 127))
    private val secondaryColor = csetting("SecondaryColor", ColorRGB(255, 135, 230))
    private val dockingHSetting = msetting("DockingH", HAlign.LEFT)
    private val dockingVSetting = msetting("DockingV", VAlign.TOP)
    private var dockingH = (dockingHSetting.value as HAlign)
    private var dockingV = (dockingVSetting.value as VAlign)

    private enum class Mode {
        LEFT_TAG,
        RIGHT_TAG,
        FRAME
    }

    @Suppress("UNUSED")
    private enum class SortingMode(val comparator: Comparator<IModule>) {
        LENGTH(compareByDescending { it.textLine.getWidth() }),
        ALPHABET(compareBy { it.moduleName }),
        CATEGORY(compareBy { it.category?.ordinal })
    }

    private var cacheWidth = 20.0f
    private var cacheHeight = 20.0f

    private val textLineMap = Int2ObjectOpenHashMap<TextComponent.TextLine>()

    private var sortedModuleList: MutableList<IModule> = ArrayList()
    private var prevToggleMap = ArrayMap<ModuleToggleFlag>()
    private val toggleMap by AsyncCachedValue(1L, TimeUnit.SECONDS) {
        ArrayMap<ModuleToggleFlag>().apply {
            ModuleManager.moduleLists.forEach {
                this[it.id] = prevToggleMap[it.id] ?: ModuleToggleFlag(it)
            }
            prevToggleMap = this
        }
    }

    init {
        safeParallelListener<TickEvent.Post> {
            sortedModuleList = ModuleManager.getModules().toMutableList()
            sortingMode = (sortingMode0.value as SortingMode)
            mode = (mode0.value as Mode)
            dockingH = (dockingHSetting.value as HAlign)
            dockingV = (dockingVSetting.value as VAlign)
            for ((id, flag) in toggleMap) {
                flag.update()
                if (flag.progress <= 0.0f) continue
                textLineMap[id] = flag.module.newTextLine()
            }

            cacheWidth = sortedModuleList.maxOfOrNull {
                if (toggleMap[it.id]?.value == true) it.textLine.getWidth() + 4.0f
                else 20.0f
            }?.let {
                max(it, 20.0f)
            } ?: 20.0f

            cacheHeight = max(toggleMap.values.sumOfFloat { it.displayHeight }, 20.0f)
            width = cacheWidth.toInt()
            height = cacheHeight.toInt()
        }

        onRender2DMc {
            GlStateManager.pushMatrix()

            GlStateManager.translate(width * dockingH.multiplier, 0.0f, 0.0f)
            if (dockingV == VAlign.BOTTOM) {
                GlStateManager.translate(0.0f, height - (MainFontRenderer.getHeight() + 2.0f), 0.0f)
            } else if (dockingV == VAlign.TOP) {
                GlStateManager.translate(0.0f, -1.0f, 0.0f)
            }

            if (dockingH == HAlign.LEFT) {
                GlStateManager.translate(-1.0f, 0.0f, 0.0f)
            }

            when (mode) {
                Mode.LEFT_TAG -> {
                    if (dockingH == HAlign.LEFT) {
                        GlStateManager.translate(2.0f, 0.0f, 0.0f)
                    }
                }

                Mode.RIGHT_TAG -> {
                    if (dockingH == HAlign.RIGHT) {
                        GlStateManager.translate(-2.0f, 0.0f, 0.0f)
                    }
                }

                else -> {
                    // 0x22 cute catgirl owo
                }
            }

            drawModuleList()

            GlStateManager.popMatrix()
        }
    }

    private fun drawModuleList() {
        if (rainbow.value) {
            val lengthMs = rainbowLength.value * 1000.0f
            val timedHue = System.currentTimeMillis() % lengthMs.toLong() / lengthMs
            var index = 0

            for (module in sortedModuleList) {
                val timedFlag = toggleMap[module.id] ?: continue
                val progress = timedFlag.progress

                if (progress <= 0.0f) continue

                GlStateManager.pushMatrix()

                val hue = timedHue + indexedHue.value * 0.05f * index
                val color = ColorUtils.hsbToRGB(hue, saturation.value, brightness.value)

                val textLine = module.textLine
                val textWidth = textLine.getWidth()
                val animationXOffset = textWidth * dockingH.offset * (1.0f - progress)
                val stringPosX = textWidth * dockingH.multiplier
                val margin = 2.0f * dockingH.offset

                var yOffset = timedFlag.displayHeight

                GlStateManager.translate(animationXOffset - margin - stringPosX, 0.0f, 0.0f)

                when (mode) {
                    Mode.LEFT_TAG -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, ColorRGB(frameColor.value))
                        RenderUtils2D.drawRectFilled(-4.0f, 0.0f, -2.0f, yOffset, color)
                    }

                    Mode.RIGHT_TAG -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, ColorRGB(frameColor.value))
                        RenderUtils2D.drawRectFilled(textWidth + 2.0f, 0.0f, textWidth + 4.0f, yOffset, color)
                    }

                    Mode.FRAME -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, ColorRGB(frameColor.value))
                    }
                }

                module.newTextLine(color).drawLine(progress, HAlign.LEFT)

                if (dockingV == VAlign.BOTTOM) yOffset *= -1.0f
                GlStateManager.popMatrix()
                GlStateManager.translate(0.0f, yOffset, 0.0f)
                index++
            }
        } else {
            val color = secondaryColor.value
            for (module in sortedModuleList) {
                val timedFlag = toggleMap[module.id] ?: continue
                val progress = timedFlag.progress

                if (progress <= 0.0f) continue

                GlStateManager.pushMatrix()

                val textLine = module.textLine
                val textWidth = textLine.getWidth()
                val animationXOffset = textWidth * dockingH.offset * (1.0f - progress)
                val stringPosX = textWidth * dockingH.multiplier
                val margin = 2.0f * dockingH.offset

                var yOffset = timedFlag.displayHeight

                GlStateManager.translate(animationXOffset - margin - stringPosX, 0.0f, 0.0f)

                when (mode) {
                    Mode.LEFT_TAG -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, ColorRGB(frameColor.value))
                        RenderUtils2D.drawRectFilled(-4.0f, 0.0f, -2.0f, yOffset, ColorRGB(color))
                    }

                    Mode.RIGHT_TAG -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, ColorRGB(frameColor.value))
                        RenderUtils2D.drawRectFilled(textWidth + 2.0f, 0.0f, textWidth + 4.0f, yOffset, ColorRGB(color))
                    }

                    Mode.FRAME -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, ColorRGB(frameColor.value))
                    }
                }

                textLine.drawLine(progress, HAlign.LEFT)

                if (dockingV == VAlign.BOTTOM) yOffset *= -1.0f
                GlStateManager.popMatrix()
                GlStateManager.translate(0.0f, yOffset, 0.0f)
            }
        }
    }

    private val IModule.textLine
        get() = textLineMap.getOrPut(this.id) {
            this.newTextLine()
        }

    private fun IModule.newTextLine(color: ColorRGB = ColorRGB(secondaryColor.value)) =
        TextComponent.TextLine(" ").apply {
            add(TextComponent.TextElement(moduleName.toString(), color))
            getHudInfo()?.let {
                if (it.isNotBlank()) {
                    add(
                        TextComponent.TextElement(
                            "${TextFormatting.GRAY format "["}${it}${TextFormatting.GRAY format "]"}",
                            ColorRGB(255, 255, 255)
                        )
                    )
                }
            }
            if (dockingH == HAlign.RIGHT) reverse()
        }

    private val TimedFlag<Boolean>.displayHeight
        get() = (MainFontRenderer.getHeight() + 2.0f) * progress

    private val TimedFlag<Boolean>.progress
        get() = if (value) {
            Easing.OUT_CUBIC.inc(Easing.toDelta(lastUpdateTime, 300L))
        } else {
            Easing.IN_CUBIC.dec(Easing.toDelta(lastUpdateTime, 300L))
        }

    private class ModuleToggleFlag(val module: IModule) : TimedFlag<Boolean>(module.state) {
        fun update() {
            value = module.state
        }
    }

    private val IModule.state: Boolean
        get() = this.isEnabled && this.isShownOnArrayStatic

    init {
        x = -2
        y = 2
        dockingH = HAlign.RIGHT
    }

}