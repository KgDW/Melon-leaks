package dev.zenhao.melon.manager

import dev.zenhao.melon.utils.font.CFont
import dev.zenhao.melon.utils.font.CFontRenderer
import java.awt.Font

object FontManager {
    var fonts: CFontRenderer? = null

    fun onInit() {
        fonts = CFontRenderer(CFont.CustomFont("/assets/fonts/LexendDeca-Regular.ttf", 20f, Font.BOLD), true, false)
    }
}