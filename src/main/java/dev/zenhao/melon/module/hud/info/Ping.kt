package dev.zenhao.melon.module.hud.info

import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.utils.chat.ChatUtil
import dev.zenhao.melon.utils.math.deneb.LagCompensator
import java.awt.Color

@HUDModule.Info(name = "Ping", x = 170, y = 170, width = 100, height = 10, category = Category.HUD)
class Ping : HUDModule() {
    override fun onRender() {
        val fontColor = Color(
            GuiManager.getINSTANCE().red / 255f,
            GuiManager.getINSTANCE().green / 255f,
            GuiManager.getINSTANCE().blue / 255f,
            1f
        ).rgb
        val privatePingValue = LagCompensator.globalInfoPingValue()
        val Final = "Ping " + ChatUtil.SECTIONSIGN + "f" + privatePingValue
        fontRenderer.drawString(Final, x + 2, y + 4, fontColor)
        width = fontRenderer.getStringWidth(Final) + 4
    }
}