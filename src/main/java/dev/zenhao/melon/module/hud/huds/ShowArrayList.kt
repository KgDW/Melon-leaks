package dev.zenhao.melon.module.hud.huds

import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.IModule
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager.getModules
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.color.ColorUtil
import dev.zenhao.melon.utils.gl.AnimationUtil
import dev.zenhao.melon.utils.gl.MelonTessellator.drawRect
import melon.system.render.font.renderer.MainFontRenderer
import melon.system.util.color.ColorRGB
import net.minecraft.client.gui.ScaledResolution
import java.awt.Color
import kotlin.math.max

@HUDModule.Info(name = "ArrayList", x = 50, y = 50, width = 100, height = 100)
object ShowArrayList : HUDModule() {
    private var scale = fsetting("Scale", 1f, 1f, 5f)
    private var drawBG = bsetting("BlurBackground", true)
    private var anim = bsetting("StressAnimation", false)
    var animationSpeed = dsetting("AnimationSpeed", 3.5, 0.0, 5.0)
    private var sideLine = bsetting("SideLine", false)
    private var sideLineWidth = isetting("SideWidth", -1, -1, 5).b(sideLine)
    private var sideColor = csetting("SideLineColor", Color(231, 13, 103)).b(sideLine)
    private var sideAlpha = isetting("SideAlpha", 80, 1, 255).b(sideLine)
    private var mode = msetting("Mode", Mode.Rainbow)
    private var color = csetting("Color", Color(210, 100, 165)).m(mode, Mode.Custom)
    var toggleList: MutableMap<IModule, Long> = HashMap()

    private fun getArrayList(module: IModule): String {
        return module.moduleName + if (module.getHudInfo() == null || module.getHudInfo() == "") "" else " " + ChatUtil.SECTIONSIGN + "7" + (if (module.getHudInfo() == "" || module.getHudInfo() == null) "" else "[") + ChatUtil.SECTIONSIGN + "f" + module.getHudInfo() + '\u00a7' + "7" + if (module.getHudInfo() == "") "" else "]"
    }

    init {
        onRender2DMc {
            var count = 0.0
            val screenWidth = ScaledResolution(mc).scaledWidth
            val screenHeight = ScaledResolution(mc).scaledHeight
            getModules().stream().filter { it.isEnabled || toggleList.contains(it) }
                .sorted(Comparator.comparing {
                    MainFontRenderer.getWidth(getArrayList(it)) * -1
                }).forEach { module ->
                    if ((module as Module).isShownOnArray) {
                        val modText = getArrayList(module)
                        val modWidth = MainFontRenderer.getWidth(modText)
                        if (module.remainingAnimation < modWidth) {
                            module.remainingAnimation = AnimationUtil.moveTowards(
                                module.remainingAnimation,
                                modWidth + 1f,
                                (0.01f + animationSpeed.value / 30).toFloat(),
                                0.1f,
                                anim.value
                            )
                        }
                        if (module.remainingAnimation > modWidth) {
                            module.remainingAnimation = modWidth
                            toggleList.remove(module)
                        }

                        if (module.remainingAnimation >= modWidth && toggleList.contains(module)) {
                            module.remainingAnimation = AnimationUtil.moveTowards(
                                module.remainingAnimation,
                                modWidth + 1f,
                                (0.01f + animationSpeed.value / 30).toFloat(),
                                0.1f,
                                anim.value
                            ) * -1
                        }
                        if (toggleList.contains(module)) {
                            //module.remainingAnimation -= (modWidth / module.remainingAnimation) * (animationSpeed.value.toFloat() / 10)
                            if (System.currentTimeMillis() - toggleList[module]!! > 0) {
                                //ChatUtil.sendMessage("Remove Debug")
                                toggleList.remove(module)
                            }
                        }

                        //RenderUtils.drawRect(x - modWidth - 4, this.y + (10 * count), x, y, new Color(255, 197, 237, 80));
                        val yOffset = (if (toggleList.containsKey(module)) {
                            if (y > screenHeight / 2) {
                                -1
                            } else {
                                1
                            } * module.remainingAnimation
                        } else {
                            0
                        }).toInt()
                        if (x < screenWidth / 2) {
                            //Left
                            if (drawBG.value) {
                                drawRect(
                                    (x - 1 - modWidth + module.remainingAnimation + yOffset).toInt().toFloat(),
                                    (y + (10 + yOffset) * count).toFloat(),
                                    (x - 2 + module.remainingAnimation + yOffset).toInt().toFloat(),
                                    (y + 10 * count + 10 + yOffset).toFloat(),
                                    Color(0, 0, 0, 70).rgb
                                )
                            }
                            MainFontRenderer.drawString(
                                modText,
                                (x - 2 - modWidth + module.remainingAnimation + yOffset).toInt().toFloat(),
                                (y + (10 + yOffset) * count).toFloat(),
                                ColorRGB(generateColor()),
                                scale.value,
                                true
                            )
                        } else {
                            //Right
                            if (drawBG.value) {
                                drawRect(
                                    (x - module.remainingAnimation - 2 + yOffset).toInt().toFloat(),
                                    (y + (10 + yOffset) * count).toFloat(),
                                    (x - module.remainingAnimation + modWidth + yOffset).toInt().toFloat(),
                                    (y + 10 * count + 10 + yOffset).toFloat(),
                                    Color(0, 0, 0, 70).rgb
                                )
                            }
                            if (sideLine.value) {
                                val sColor =
                                    Color(
                                        sideColor.value.red,
                                        sideColor.value.green,
                                        sideColor.value.blue,
                                        sideAlpha.value
                                    )
                                drawRect(
                                    (x - module.remainingAnimation - 2 + yOffset).toInt().toFloat(),
                                    (y + (10 + yOffset) * count).toFloat(),
                                    (x - module.remainingAnimation + sideLineWidth.value + yOffset).toInt().toFloat(),
                                    (y + 10 * count + 10 + yOffset).toFloat(),
                                    sColor.rgb
                                )
                            }
                            MainFontRenderer.drawString(
                                modText,
                                (x - module.remainingAnimation + yOffset).toInt().toFloat(),
                                (y + (10 + yOffset) * count).toFloat(),
                                ColorRGB(generateColor()),
                                scale.value,
                                true
                            )
                        }
                        if (y > screenHeight / 2) {
                            count-- + yOffset
                        } else {
                            count++ + yOffset
                        }
                    }
                    width = if (x < screenWidth / 2) {
                        max(75, MainFontRenderer.getWidth(getArrayList(module)).toInt())
                    } else {
                        max(-75, -MainFontRenderer.getWidth(getArrayList(module)).toInt())
                    }
                }
            height = ((MainFontRenderer.getHeight().toInt() + 1) * count).toInt()
        }
    }

    private fun generateColor(): Color {
        val fontColor = Color(
            GuiManager.getINSTANCE().red / 255f,
            GuiManager.getINSTANCE().green / 255f,
            GuiManager.getINSTANCE().blue / 255f,
            1f
        )
        val custom = Color(color.value.red, color.value.green, color.value.blue)
        when (mode.value) {
            Mode.Rainbow -> return ColorUtil.staticRainbow()
            Mode.GuiSync -> return fontColor
            Mode.Custom -> return custom
        }
        return Color.WHITE
    }

    enum class Mode {
        Rainbow, GuiSync, Custom
    }
}