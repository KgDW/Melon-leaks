package dev.zenhao.melon.module.hud.huds

import dev.zenhao.melon.manager.GuiManager
import dev.zenhao.melon.module.Category
import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.setting.StringSetting
import dev.zenhao.melon.utils.font.CFont
import dev.zenhao.melon.utils.font.CFontRenderer
import java.awt.Color
import java.awt.Font

@HUDModule.Info(name = "Welcomer", x = 120, y = 20, width = 100, height = 20, category = Category.HUD)
class Welcomer : HUDModule() {
    var fonts = CFontRenderer(CFont.CustomFont("/assets/fonts/LemonMilk.ttf", 20f, Font.PLAIN), true, false)
    private var text: StringSetting = ssetting("Text", "Nice To Meet You @Player OwO")

    override fun onRender() {
        val fontColor = Color(
            GuiManager.getINSTANCE().red / 255f,
            GuiManager.getINSTANCE().green / 255f,
            GuiManager.getINSTANCE().blue / 255f,
            1f
        ).rgb
        val txt = text.value.toString().replace("@Player", mc.player.name)
        fonts.drawString(txt, x.toFloat(), y.toFloat(), fontColor)
    }
}