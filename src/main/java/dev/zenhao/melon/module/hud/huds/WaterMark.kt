package dev.zenhao.melon.module.hud.huds

import dev.zenhao.melon.module.HUDModule
import dev.zenhao.melon.utils.font.CFont
import dev.zenhao.melon.utils.font.RFontRenderer
import org.lwjgl.opengl.GL11
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer

@HUDModule.Info(name = "WaterMark", x = 20, y = 20)
class WaterMark : HUDModule() {
    private var fonts = CopyOnWriteArrayList<RFontRenderer>()
    private var text = ssetting("ViewText", "Melon <3")
    private var Scala = fsetting("Scala", 1.0f, 0.0f, 3.0f)
    private var customFont = RFontRenderer(CFont.CustomFont("/assets/fonts/Goldman.ttf", 47.0f, 0), true, false)

    override fun onRender() {
        if (!fonts.contains(customFont)) {
            fonts.add(customFont)
        }
        fonts.forEach(Consumer { f: RFontRenderer ->
            GL11.glPushMatrix()
            GL11.glTranslated(x.toDouble(), y.toDouble(), 0.0)
            GL11.glScaled(Scala.value.toDouble(), Scala.value.toDouble(), 0.0)
            f.drawString(text.value, 0f, 0f, 6.0f, 1.0f, 1.0f, 50, 255)
            GL11.glPopMatrix()
            width = (f.getStringWidth(text.value).toFloat() * Scala.value).toInt()
            height = (f.height.toFloat() * Scala.value).toInt()
        })
    }
}