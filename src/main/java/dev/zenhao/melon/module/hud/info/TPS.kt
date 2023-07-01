package dev.zenhao.melon.module.hud.info

import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.math.deneb.LagCompensator
import java.awt.Color

@HUDModule.Info(name = "TPS", x = 160, y = 160, width = 100, height = 10, category = Category.HUD)
class TPS : HUDModule() {
    override fun onRender() {
        val fontColor = Color(
            GuiManager.getINSTANCE().red / 255f,
            GuiManager.getINSTANCE().green / 255f,
            GuiManager.getINSTANCE().blue / 255f,
            1f
        ).rgb
        val Final = "TPS " + ChatUtil.SECTIONSIGN + "f" + String.format("%.2f", LagCompensator.INSTANCE.tickRate)
        fontRenderer.drawString(Final, x + 2, y + 4, fontColor)
        width = fontRenderer.getStringWidth(Final) + 4
    }
}