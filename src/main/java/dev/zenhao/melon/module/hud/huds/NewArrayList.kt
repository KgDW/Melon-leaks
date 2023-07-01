package dev.zenhao.melon.module.hud.huds

import dev.zenhao.melon.manager.FontManager
import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.module.IModule
import dev.zenhao.melon.module.Module
import dev.zenhao.melon.module.ModuleManager.getModules
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.color.ColorUtil
import dev.zenhao.melon.utils.gl.MelonTessellator
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11
import java.awt.Color
import java.util.stream.Collectors

@HUDModule.Info(name = "NewArrayList", x = 50, y = 50, width = 100, height = 100)
class NewArrayList : HUDModule() {
    private var customFont = bsetting("CustomFont", false)
    private var drawBG = bsetting("BlurBackground", true)
    private var speed = isetting("MoveSpeed", 15,1,30)
    private var sideLine = bsetting("SideLine", false)
    private var sideLineWidth = isetting("SideWidth", -1, -1, 5).b(sideLine)
    private var sideColor = csetting("SideLineColor", Color(231, 13, 103)).b(sideLine)
    private var sideAlpha = isetting("SideAlpha", 80, 1, 255).b(sideLine)
    private var count = 0
    private var mode = msetting("Mode", Mode.Rainbow)
    private var color = csetting("Color", Color(255, 198, 203)).m(mode, Mode.Custom)
    private var lastListModule: List<IModule>? = ArrayList()
    private var toAddMoveList: MutableMap<IModule, Int> = HashMap()
    private var toRemoveMoveList: MutableMap<IModule, Int> = HashMap()
    private var delayed = 0

    private fun getArrayList(module: IModule): String {
        return module.moduleName + if (module.getHudInfo() == null || module.getHudInfo() == "") "" else " " + ChatUtil.SECTIONSIGN + "7" + (if (module.getHudInfo() == "" || module.getHudInfo() == null) "" else "[") + ChatUtil.SECTIONSIGN + "f" + module.getHudInfo() + '\u00a7' + "7" + if (module.getHudInfo() == "") "" else "]"
    }

    override fun onRender() {
        count = 0
        val screenWidth = ScaledResolution(mc).scaledWidth
        val allModule = getModules().stream().sorted(Comparator.comparing { module: IModule ->
            if (customFont.value) FontManager.fonts!!.getStringWidth(
                getArrayList(module)
            ) * -1 else mc.fontRenderer.getStringWidth(getArrayList(module)) * -1
        }).collect(
            Collectors.toList()
        )
        delayed += 1

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glShadeModel(GL11.GL_SMOOTH)

        val enableModuleList = getModules().stream()
            .filter { it.isEnabled }
            .filter { it.isShownOnArrayStatic }
            .sorted(Comparator.comparing { module: IModule ->
                if (customFont.value) FontManager.fonts!!.getStringWidth(
                    getArrayList(module)
                ) * -1 else mc.fontRenderer.getStringWidth(getArrayList(module)) * -1
            }).collect(Collectors.toList())
        val maxOffset = 25
        for (mod in enableModuleList) {
            if (!lastListModule!!.contains(mod)) {
                toRemoveMoveList.remove(mod)
                toAddMoveList[mod] = maxOffset
            }
        }
        for (value in lastListModule!!) {
            if (!enableModuleList.contains(value)) {
                toAddMoveList.remove(value)
                toRemoveMoveList[value] = 0
            }
        }
        val moveSpeed = speed.value
        for (module in allModule) {
            //Check Should Render
            if ((toRemoveMoveList.containsKey(module) || module.isEnabled) && !module.isHidden() && (module as Module).isShownOnArray) {
                val move = true
                if (!move) {
                    if (toRemoveMoveList.containsKey(module)) continue
                }
                val modText = getArrayList(module)
                var offset = 0
                var toggleOffset = 0
                if (toAddMoveList.containsKey(module)) {
                    offset = toAddMoveList[module]!!
                    if (offset <= 0) {
                        toAddMoveList.remove(module)
                    } else {
                        if (delayed >= 20 / moveSpeed) toAddMoveList[module] = offset - 1
                    }
                } else if (toRemoveMoveList.containsKey(module)) {
                    offset = toRemoveMoveList[module]!!
                    toggleOffset = offset
                    if (offset >= maxOffset) {
                        toRemoveMoveList.remove(module)
                    } else if (delayed >= 20 / moveSpeed) {
                        toRemoveMoveList[module] = offset + 1
                    }
                }
                val screenWidthScaled = ScaledResolution(mc).scaledWidth.toFloat()
                val modWidth =
                    (if (customFont.value) FontManager.fonts!!.getStringWidth(getArrayList(module)) else mc.fontRenderer.getStringWidth(
                        getArrayList(module)
                    )).toFloat()
                val movingVal = if (move) offset else 0
                val toggleVal = if (move) toggleOffset else 0
                if (x < screenWidthScaled / 2) {
                    if (drawBG.value) {
                        MelonTessellator.drawRect(
                            (x - movingVal - 2).toFloat(),
                            (y + toggleVal + 10 * count).toFloat(),
                            (x + modWidth - movingVal),
                            (y + toggleVal + 10 * count + 10).toFloat(),
                            Color(0, 0, 0, 70).rgb
                        )
                    }
                    if (sideLine.value) {
                        val sColor =
                            Color(sideColor.value.red, sideColor.value.green, sideColor.value.blue, sideAlpha.value)
                        MelonTessellator.drawRect(
                            (x - 1 - sideLineWidth.value + movingVal).toFloat(),
                            (y + toggleVal + 10 * count).toFloat(),
                            (x - 2 + movingVal).toFloat(),
                            (y + toggleVal + 10 * count + 10).toFloat(),
                            sColor.rgb
                        )
                    }
                    if (customFont.value) {
                        FontManager.fonts!!.drawString(
                            modText,
                            (x + movingVal).toFloat(),
                            (y + toggleVal + 10 * count).toFloat(),
                            generateColor()
                        )
                    } else {
                        mc.fontRenderer.drawStringWithShadow(
                            modText,
                            (x + movingVal).toFloat(),
                            (y + toggleVal + 10 * count).toFloat(),
                            generateColor()
                        )
                    }
                } else {
                    if (drawBG.value) {
                        MelonTessellator.drawRect(
                            (x - 1 - modWidth + movingVal).toInt().toFloat(),
                            (y + toggleVal + 10 * count).toFloat(),
                            (x - 2 + movingVal).toFloat(),
                            (y + toggleVal + 10 * count + 10).toFloat(),
                            Color(0, 0, 0, 70).rgb
                        )
                    }
                    if (sideLine.value) {
                        val sColor =
                            Color(sideColor.value.red, sideColor.value.green, sideColor.value.blue, sideAlpha.value)
                        MelonTessellator.drawRect(
                            (x - movingVal - 2).toFloat(),
                            (y + toggleVal + 10 * count).toFloat(),
                            (x - movingVal + sideLineWidth.value).toFloat(),
                            (y + toggleVal + 10 * count + 10).toFloat(),
                            sColor.rgb
                        )
                    }
                    if (customFont.value) {
                        val textWidth = FontManager.fonts!!.getStringWidth(modText)
                        FontManager.fonts!!.drawString(
                            modText,
                            (x - 2 - textWidth + movingVal).toFloat(),
                            (y + toggleVal + 10 * count).toFloat(),
                            generateColor()
                        )
                    } else {
                        val textWidth = mc.fontRenderer.getStringWidth(modText)
                        mc.fontRenderer.drawStringWithShadow(
                            modText,
                            (x - 2 - textWidth + movingVal).toFloat(),
                            (y + toggleVal + 10 * count).toFloat(),
                            generateColor()
                        )
                    }
                }
                count++
                width = if (x < screenWidth / 2) {
                    75
                } else {
                    -75
                }
                height = (FontManager.fonts!!.height + 1) * count
            }
        }
        if (delayed >= 20 / moveSpeed) delayed = 0
        lastListModule = enableModuleList
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glDisable(GL11.GL_BLEND)
    }

    fun generateColor(): Int {
        val fontColor = Color(
            GuiManager.getINSTANCE().red / 255f,
            GuiManager.getINSTANCE().green / 255f,
            GuiManager.getINSTANCE().blue / 255f,
            1f
        ).rgb
        val custom = color.value.rgb
        when (mode.value) {
            Mode.Rainbow -> return ColorUtil.staticRainbow().rgb
            Mode.GuiSync -> return fontColor
            Mode.Custom -> return custom
        }
        return -1
    }

    enum class Mode {
        Rainbow, GuiSync, Custom
    }
}