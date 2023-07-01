package dev.zenhao.melon.module.hud.huds

import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.utils.chat.ChatUtil
import java.awt.Color

@HUDModule.Info(name = "CoordsHUD", x = 150, y = 150, width = 100, height = 10)
class CoordsHUD : HUDModule() {
    override fun onRender() {
        val fontColor = Color(
            GuiManager.getINSTANCE().red / 255f,
            GuiManager.getINSTANCE().green / 255f,
            GuiManager.getINSTANCE().blue / 255f,
            1f
        ).rgb
        val inHell = mc.player.dimension == -1
        val f = if (!inHell) 0.125f else 8.0f
        val posX = String.format("%.1f", mc.player.posX)
        val posY = String.format("%.1f", mc.player.posY)
        val posZ = String.format("%.1f", mc.player.posZ)
        val hposX = String.format("%.1f", mc.player.posX * f.toDouble())
        val hposZ = String.format("%.1f", mc.player.posZ * f.toDouble())
        val ow = "$posX, $posY, $posZ"
        val nether = "$hposX, $posY, $hposZ"
        val Final =
            ChatUtil.SECTIONSIGN.toString() + "rXYZ " + ChatUtil.SECTIONSIGN + "f" + ow + ChatUtil.SECTIONSIGN + "r [" + ChatUtil.SECTIONSIGN + "f" + nether + ChatUtil.SECTIONSIGN + "r]"
        fontRenderer.drawString(Final, x + 2, y + 4, fontColor)
        width = fontRenderer.getStringWidth(Final) + 4
    }
}