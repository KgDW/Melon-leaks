package dev.zenhao.melon.module.hud.info

import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.utils.chat.ChatUtil
import net.minecraft.client.Minecraft
import java.awt.Color

@HUDModule.Info(name = "FPS", x = 160, y = 160, width = 100, height = 10, category = Category.HUD)
class FPS : HUDModule() {
    override fun onRender() {
        val fontColor = Color(
            GuiManager.getINSTANCE().red / 255f,
            GuiManager.getINSTANCE().green / 255f,
            GuiManager.getINSTANCE().blue / 255f,
            1f
        ).rgb
        val Final = "FPS " + ChatUtil.SECTIONSIGN + "f" + Minecraft.getDebugFPS()
        fontRenderer.drawString(Final, x + 2, y + 4, fontColor)
        width = fontRenderer.getStringWidth(Final) + 4
    }
}