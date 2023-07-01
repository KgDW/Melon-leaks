package dev.zenhao.melon.module.hud.info

import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import java.awt.Color
import java.util.*

/**
 * Created by B_312 on 01/03/21
 */
@HUDModule.Info(name = "Server", x = 160, y = 160, width = 100, height = 10, category = Category.HUD)
class Server : HUDModule() {
    override fun onRender() {
        val fontColor = Color(
            GuiManager.getINSTANCE().red / 255f,
            GuiManager.getINSTANCE().green / 255f,
            GuiManager.getINSTANCE().blue / 255f,
            1f
        ).rgb
        val Final =
            "IP " + "\u00a7f" + if (mc.isSingleplayer) "Single Player" else Objects.requireNonNull(mc.getCurrentServerData())!!.serverIP.lowercase(
                Locale.getDefault()
            )
        fontRenderer.drawString(Final, x + 2, y + 4, fontColor)
        width = fontRenderer.getStringWidth(Final) + 4
    }
}