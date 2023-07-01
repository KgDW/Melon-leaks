package dev.zenhao.melon.module.hud.info

import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.math.InfoCalculator
import java.awt.Color

/**
 * Created by B_312 on 01/03/21
 */
@HUDModule.Info(name = "Speed", x = 140, y = 160, width = 100, height = 10, category = Category.HUD)
class SpeedHud : HUDModule() {
    override fun onRender() {
        val fontColor = Color(
            GuiManager.getINSTANCE().red / 255f,
            GuiManager.getINSTANCE().green / 255f,
            GuiManager.getINSTANCE().blue / 255f,
            1f
        ).rgb
        val Final = "Speed " + ChatUtil.SECTIONSIGN + "f" + InfoCalculator.speed(true, mc) + " km/h"
        fontRenderer.drawString(Final, x + 2, y + 4, fontColor)
        width = fontRenderer.getStringWidth(Final) + 4
    }
}